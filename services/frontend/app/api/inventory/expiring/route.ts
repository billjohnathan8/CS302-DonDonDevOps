import { NextResponse } from "next/server"
import { asMenuProduct, listInventoryProducts, resolveInventoryError } from "@/lib/inventory-service"
import type { ExpiringItem } from "@/lib/types"

const EXPIRY_WINDOW_DAYS = Number(process.env.EXPIRY_LOOKAHEAD_DAYS ?? 7)

export async function GET() {
  const now = new Date()
  const windowLimit = new Date(now.getTime() + EXPIRY_WINDOW_DAYS * 24 * 60 * 60 * 1000)

  try {
    const inventoryProducts = await listInventoryProducts()

    const expiringItems: ExpiringItem[] = inventoryProducts
      .filter((product) => {
        if (!product.expiryDate) return false
        const expiry = new Date(product.expiryDate)
        return expiry >= now && expiry <= windowLimit
      })
      .map((product) => {
        const expiry = new Date(product.expiryDate!)
        const daysUntilExpiry = Math.ceil((expiry.getTime() - now.getTime()) / (24 * 60 * 60 * 1000))
        const menuProduct = asMenuProduct(product)
        menuProduct.stock = { qty: product.stock }

        return {
          product: menuProduct,
          expiresOn: expiry.toISOString(),
          qty: product.stock,
          currentPrice: product.priceInSGD,
          daysUntilExpiry,
        }
      })

    return NextResponse.json(expiringItems)
  } catch (error) {
    const { status, body } = resolveInventoryError(error, "Unable to load expiring inventory.")
    return NextResponse.json(body, { status })
  }
}
