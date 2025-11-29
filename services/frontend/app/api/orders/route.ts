import { NextResponse } from "next/server"
import { STUB_ORDERS } from "@/lib/admin-seed-data"
import type { CartLine, CartTotals, Order } from "@/lib/types"
import { generateLineId, generateOrderNumber } from "@/lib/cart-utils"
import { createOrchestratedOrder, resolveOrderOrchestratorError } from "@/lib/order-orchestrator-service"
import { getInventoryProduct, resolveInventoryError } from "@/lib/inventory-service"

const orders: Order[] = [...STUB_ORDERS]

export async function POST(request: Request) {
  try {
    const body = await request.json()
    const normalizedItems = normalizeCartItems(body.items)

    if (normalizedItems.length === 0) {
      return NextResponse.json(
        { error: "CartInvalid", message: "Provide at least one item with quantity greater than zero." },
        { status: 400 },
      )
    }

    const mergedItems = mergeCartItems(normalizedItems)
    if (mergedItems.length === 0) {
      return NextResponse.json(
        { error: "CartInvalid", message: "Only items with quantities greater than zero can be ordered." },
        { status: 400 },
      )
    }

    let orderItems: CartLine[]
    try {
      orderItems = await hydrateOrderItems(mergedItems)
    } catch (error) {
      const { status, body } = resolveInventoryError(error, "Unable to load items from inventory.")
      return NextResponse.json(body, { status })
    }

    const totals = calculateTotals(orderItems)

    const order: Order = {
      id: `order_${Date.now()}`,
      number: generateOrderNumber(),
      origin: "kiosk",
      items: orderItems,
      totals,
      status: "placed",
      createdAt: new Date().toISOString()
    }

    orders.push(order)

    const paymentInfo = resolvePaymentInfo(body)
    const orchestratorPayload = {
      items: mergedItems.map((item) => ({
        product_id: item.productId,
        quantity: item.quantity,
      })),
      payment_info: paymentInfo,
    }

    const orchestratorOrder = await createOrchestratedOrder(orchestratorPayload)

    return NextResponse.json({
      ...order,
      orchestratorOrder,
    })
  } catch (error) {
    const resolved = resolveOrderOrchestratorError(error, "Unable to place order at this time.")
    return NextResponse.json(resolved.body, { status: resolved.status })
  }
}

export async function GET() {
  // Return all orders for admin order history
  return NextResponse.json(orders)
}

type CartSubmissionItem = {
  productId: string
  quantity: number
}

function normalizeCartItems(rawItems: unknown): CartSubmissionItem[] {
  if (!Array.isArray(rawItems)) return []
  const normalized: CartSubmissionItem[] = []

  for (const raw of rawItems) {
    if (!raw || typeof raw !== "object") continue
    const data = raw as Record<string, unknown>
    const productIdRaw =
      typeof data.productId === "string"
        ? data.productId
        : typeof data.product_id === "string"
          ? data.product_id
          : typeof data.product_Id === "string"
            ? data.product_Id
            : undefined
    const productId = productIdRaw?.trim()
    if (!productId) continue

    const quantityCandidate =
      typeof data.quantity === "number" ? data.quantity : typeof data.qty === "number" ? data.qty : undefined
    if (!quantityCandidate || quantityCandidate <= 0) continue

    normalized.push({
      productId,
      quantity: Math.trunc(quantityCandidate),
    })
  }

  return normalized
}

function mergeCartItems(items: CartSubmissionItem[]) {
  const merged = new Map<string, number>()

  for (const item of items) {
    const current = merged.get(item.productId) ?? 0
    merged.set(item.productId, current + Math.max(0, item.quantity))
  }

  return Array.from(merged.entries())
    .filter(([, quantity]) => quantity > 0)
    .map(([productId, quantity]) => ({ productId, quantity }))
}

async function hydrateOrderItems(items: CartSubmissionItem[]): Promise<CartLine[]> {
  return Promise.all(
    items.map(async ({ productId, quantity }) => {
      const product = await getInventoryProduct(productId)
      const unitPrice = Number(product.priceInSGD ?? 0)
      const lineTotal = unitPrice * quantity

      return {
        lineId: generateLineId(),
        productId,
        name: product.name ?? product.productID,
        qty: quantity,
        unitPrice,
        baseUnitPrice: product.priceInSGD ?? unitPrice,
        lineTotal,
      }
    }),
  )
}

function calculateTotals(items: CartLine[]): CartTotals {
  const subtotal = items.reduce((sum, item) => sum + item.lineTotal, 0)
  return {
    subtotal,
    discounts: 0,
    total: subtotal,
  }
}

function resolvePaymentInfo(body: any) {
  const info = (body?.payment_info as Record<string, unknown>) ?? {}
  const fallback = typeof body?.paymentMethodId === "string" ? body.paymentMethodId : undefined
  const paymentMethodRaw =
    typeof info.payment_method_id === "string" ? info.payment_method_id : typeof fallback === "string" ? fallback : ""
  const payment_method_id = paymentMethodRaw.trim() || "pm_card_mastercard"

  const currencyRaw =
    typeof info.currency === "string" ? info.currency : typeof body?.currency === "string" ? body.currency : "sgd"
  const currency = currencyRaw ? currencyRaw.trim().toLowerCase() : "sgd"

  return {
    payment_method_id,
    currency: currency || "sgd",
  }
}
