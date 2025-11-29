import { NextResponse } from "next/server"
import { asMenuProduct, listInventoryProducts, resolveInventoryError } from "@/lib/inventory-service"
import type { MenuCategory, MenuResponse } from "@/lib/types"
import { listPromotions } from "@/lib/promotions-service"
import { listProductsForPromotion } from "@/lib/product-promotion-service"

const CATEGORY_NAMES = [
  "Fruits & Vegetables",
  "Meat & Seafood",
  "Bakery",
  "Dairy & Eggs",
  "Pantry Staples",
  "Snacks",
  "Frozen Foods",
  "Beverages",
  "Household Essentials",
  "Baby & Kids",
  "Pet Supplies",
  "Home & Kitchen",
  "Seasonal & Festive",
  "Daily Necessities",
] as const

const FALLBACK_CATEGORY_NAME = "Daily Necessities"
const CATEGORY_ID_MAP = new Map<string, string>(
  CATEGORY_NAMES.map((name) => [name.toLowerCase(), slugify(name)]),
)
const FALLBACK_CATEGORY_ID = CATEGORY_ID_MAP.get(FALLBACK_CATEGORY_NAME.toLowerCase()) ?? "daily-necessities"

export async function GET() {
  try {
    const inventoryProducts = await listInventoryProducts()
    const productDiscounts = await resolvePromotionDiscounts()

    const products = inventoryProducts.map((inventoryProduct) => {
      const categoryId = resolveCategoryId(inventoryProduct.category)
      const product = asMenuProduct(inventoryProduct)
      product.categoryId = categoryId
      const discountRate = productDiscounts.get(product.id)
      if (discountRate) {
        const discountedPrice = Number((product.basePrice * (1 - discountRate)).toFixed(2))
        product.promoPrice = discountedPrice
        product.discountPercent = Math.round(discountRate * 100)
      }
      return product
    })

    const categories: MenuCategory[] = CATEGORY_NAMES.map((name, index) => ({
      id: CATEGORY_ID_MAP.get(name.toLowerCase())!,
      name,
      sort: index + 1,
    }))

    const response: MenuResponse = {
      categories,
      products,
    }

    return NextResponse.json(response)
  } catch (error) {
    const { status, body } = resolveInventoryError(error, "Unable to load menu data from inventory service.")
    return NextResponse.json(body, { status })
  }
}

function slugify(value: string) {
  return value
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-|-$/g, "")
    .trim() || FALLBACK_CATEGORY_ID
}

function resolveCategoryId(rawName?: string | null) {
  const normalized = rawName?.trim().toLowerCase()
  return (normalized && CATEGORY_ID_MAP.get(normalized)) ?? FALLBACK_CATEGORY_ID
}

async function resolvePromotionDiscounts() {
  const discountMap = new Map<string, number>()
  try {
    const promotions = await listPromotions()
    const now = Date.now()
    const activePromos = promotions.filter((promo) => {
      const start = Date.parse(promo.startTime)
      const end = Date.parse(promo.endTime)
      return Number.isFinite(start) && Number.isFinite(end) && start <= now && now <= end && promo.discountRate > 0
    })

    await Promise.all(
      activePromos.map(async (promo) => {
        try {
          if (!promo.id) return
          const linkedProducts = await listProductsForPromotion(promo.id)
          for (const record of linkedProducts) {
            const existing = discountMap.get(record.productId) ?? 0
            discountMap.set(record.productId, Math.max(existing, promo.discountRate))
          }
        } catch (error) {
          console.error(`[menu] Failed to load product promotions for ${promo.id}`, error)
        }
      }),
    )
  } catch (error) {
    console.warn("[menu] Unable to load promotions for menu display:", error)
  }

  return discountMap
}
