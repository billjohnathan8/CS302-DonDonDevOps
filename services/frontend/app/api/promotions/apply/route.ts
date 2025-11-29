import { NextResponse } from "next/server"
import { applyPromotions, resolvePromotionsError, normalizePromoDate } from "@/lib/promotions-service"

export async function POST(request: Request) {
  try {
    const body = await request.json()

    if (!Array.isArray(body.items) || body.items.length === 0) {
      return NextResponse.json({ error: "InvalidRequest", message: "At least one item is required." }, { status: 400 })
    }

    const items = body.items
      .map((item: Record<string, unknown>) => normalizeApplyItem(item))
      .filter((item): item is NonNullable<ReturnType<typeof normalizeApplyItem>> => Boolean(item))

    if (items.length === 0) {
      return NextResponse.json({ error: "InvalidRequest", message: "No valid items provided." }, { status: 400 })
    }

    const now = normalizePromoDate(body.now)

    const response = await applyPromotions({ now, items })

    return NextResponse.json(response)
  } catch (error) {
    const { status, body } = resolvePromotionsError(error, "Unable to apply promotions.")
    return NextResponse.json(body, { status })
  }
}

function normalizeApplyItem(item: Record<string, unknown>) {
  const productId = typeof item.productId === "string" ? item.productId : undefined
  const quantity = Number(item.quantity)
  const unitPrice = Number(item.unitPrice)

  if (!productId || !Number.isFinite(quantity) || quantity <= 0 || !Number.isFinite(unitPrice) || unitPrice < 0) {
    return undefined
  }

  return {
    productId,
    quantity: Math.trunc(quantity),
    unitPrice,
  }
}
