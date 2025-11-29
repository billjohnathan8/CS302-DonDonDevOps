import { NextResponse } from "next/server"
import { restockInventory, resolveInventoryError } from "@/lib/inventory-service"

export async function POST(request: Request) {
  try {
    const body = await request.json()
    const productId = body.productId as string
    const quantity = Number(body.quantity)
    const expiryDate = body.expiryDate as string

    if (!productId || !Number.isFinite(quantity) || quantity <= 0 || !expiryDate) {
      return NextResponse.json(
        { error: "InvalidRequest", message: "productId, quantity (> 0), and expiryDate are required." },
        { status: 400 },
      )
    }

    const normalizedQuantity = Math.trunc(quantity)
    if (normalizedQuantity <= 0) {
      return NextResponse.json(
        { error: "InvalidRequest", message: "quantity must be an integer greater than 0." },
        { status: 400 },
      )
    }

    const restocked = await restockInventory({
      productId,
      quantity: normalizedQuantity,
      expiryDate,
    })
    return NextResponse.json(restocked)
  } catch (error) {
    const { status, body } = resolveInventoryError(error, "Unable to restock inventory.")
    return NextResponse.json(body, { status })
  }
}
