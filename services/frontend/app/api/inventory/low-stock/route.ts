import { NextResponse } from "next/server"
import { asMenuProduct, listInventoryProducts, resolveInventoryError } from "@/lib/inventory-service"
import type { LowStockItem } from "@/lib/types"

const LOW_STOCK_THRESHOLD = Number(process.env.LOW_STOCK_THRESHOLD ?? 10)

export async function GET() {
  try {
    const inventoryProducts = await listInventoryProducts()
    const lowStockItems: LowStockItem[] = inventoryProducts
      .filter((product) => typeof product.stock === "number" && product.stock <= LOW_STOCK_THRESHOLD)
      .map((product) => {
        const menuProduct = asMenuProduct(product)
        menuProduct.stock = { qty: product.stock, lowStockThreshold: LOW_STOCK_THRESHOLD }
        return {
          product: menuProduct,
          stock: product.stock,
          threshold: LOW_STOCK_THRESHOLD,
          lastUpdated: new Date().toISOString(),
        }
      })

    return NextResponse.json(lowStockItems)
  } catch (error) {
    const { status, body } = resolveInventoryError(error, "Unable to load low-stock products.")
    return NextResponse.json(body, { status })
  }
}
