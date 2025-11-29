import { NextResponse } from "next/server"
import {
  linkProductToPromotion,
  listProductsForPromotion,
  resolveProductPromotionError,
} from "@/lib/product-promotion-service"
import { getInventoryProduct, resolveInventoryError } from "@/lib/inventory-service"

export async function GET(request: Request, { params }: { params: { id: string } }) {
  try {
    if (!params?.id) {
      return NextResponse.json({ error: "InvalidPromotion", message: "Promotion ID is required." }, { status: 400 })
    }

    const productRefs = await listProductsForPromotion(params.id)

    if (!Array.isArray(productRefs) || productRefs.length === 0) {
      return NextResponse.json([])
    }

    const inventoryResults = await Promise.allSettled(
      productRefs.map((ref) => getInventoryProduct(ref.productId).then(asPromotionProduct)),
    )

    const products = inventoryResults
      .filter((result): result is PromiseFulfilledResult<PromotionProduct> => result.status === "fulfilled")
      .map((result) => result.value)

    const failed = inventoryResults.filter((result) => result.status === "rejected")

    if (products.length === 0 && failed.length > 0) {
      const { body, status } = resolveInventoryError(
        failed[0].reason,
        "Unable to load linked products from the inventory service.",
      )
      return NextResponse.json(body, { status })
    }

    if (failed.length > 0) {
      console.warn(
        `[promotions] Failed to hydrate ${failed.length} promotion product(s) for ${params.id}`,
        failed.map((result) => result.status === "rejected" && result.reason),
      )
    }

    return NextResponse.json(products)
  } catch (error) {
    const { status, body } = resolveProductPromotionError(error, "Unable to load products linked to this promotion.")
    return NextResponse.json(body, { status })
  }
}

export async function POST(request: Request, { params }: { params: { id: string } }) {
  if (!params?.id) {
    return NextResponse.json({ error: "InvalidPromotion", message: "Promotion ID is required." }, { status: 400 })
  }

  try {
    const body = await request.json()
    const productIds = Array.isArray(body?.productIds) ? (body.productIds as string[]) : []
    const normalized = productIds
      .map((id) => (typeof id === "string" ? id.trim() : ""))
      .filter((id) => id)

    if (normalized.length === 0) {
      return NextResponse.json({ error: "InvalidRequest", message: "Provide at least one productId." }, { status: 400 })
    }

    const existing = await listProductsForPromotion(params.id)
    const existingSet = new Set(existing.map((record) => record.productId))
    const toCreate = normalized.filter((id) => !existingSet.has(id))

    if (toCreate.length === 0) {
      return NextResponse.json({ added: [], skipped: normalized.length, message: "Products already linked." })
    }

    const created: string[] = []
    for (const productId of toCreate) {
      const record = await linkProductToPromotion(params.id, productId)
      created.push(record.productId)
    }

    return NextResponse.json({ added: created, totalAdded: created.length })
  } catch (error) {
    const { status, body } = resolveProductPromotionError(error, "Unable to link products to this promotion.")
    return NextResponse.json(body, { status })
  }
}

type PromotionProduct = {
  productId: string
  name: string
  brand?: string | null
  stock: number
  unitPrice: number
}

function asPromotionProduct(product: Awaited<ReturnType<typeof getInventoryProduct>>): PromotionProduct {
  return {
    productId: product.productID,
    name: product.name,
    brand: product.brand,
    stock: product.stock,
    unitPrice: product.priceInSGD,
  }
}
