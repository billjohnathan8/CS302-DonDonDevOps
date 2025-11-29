import { NextResponse } from "next/server"
import { asProductAdmin, getInventoryProduct, resolveInventoryError, updateInventoryProduct } from "@/lib/inventory-service"

export async function POST(request: Request, { params }: { params: { id: string } }) {
  try {
    const { discountRate } = await request.json()
    const rate = Number(discountRate)

    if (!Number.isFinite(rate) || rate <= 0 || rate >= 1) {
      return NextResponse.json(
        { error: "InvalidDiscount", message: "discountRate must be a number between 0 and 1 (exclusive)." },
        { status: 400 },
      )
    }

    const product = await getInventoryProduct(params.id)
    const discountedPrice = Number((product.priceInSGD * (1 - rate)).toFixed(2))

    const updated = await updateInventoryProduct(params.id, { price: discountedPrice })

    return NextResponse.json({
      success: true,
      product: asProductAdmin(updated),
      discountRate: rate,
      newPrice: discountedPrice,
      message: "Discount applied successfully",
    })
  } catch (error) {
    const { status, body } = resolveInventoryError(error, "Unable to apply discount on inventory product.")
    return NextResponse.json(body, { status })
  }
}
