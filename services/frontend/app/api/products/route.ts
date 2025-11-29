import { NextResponse } from "next/server"
import {
  asProductAdmin,
  createInventoryProduct,
  listInventoryProducts,
  resolveInventoryError,
} from "@/lib/inventory-service"

export async function GET() {
  try {
    const products = await listInventoryProducts()
    return NextResponse.json(products.map(asProductAdmin))
  } catch (error) {
    const { status, body } = resolveInventoryError(error, "Unable to load inventory products.")
    return NextResponse.json(body, { status })
  }
}

export async function POST(request: Request) {
  try {
    const body = await request.json()
    const priceCandidate = Number(body.priceSGD ?? body.price)
    const stockCandidate = Number(body.stock)
    const expiryDate = normalizeExpiryDate(body.expiryDate)

    const payload = {
      name: body.name ?? "",
      brand: body.brand ?? "",
      category: body.category ?? "",
      price: Number.isFinite(priceCandidate) ? priceCandidate : 0,
      stock: Number.isFinite(stockCandidate) ? stockCandidate : 0,
      expiryDate,
    }

    const created = await createInventoryProduct(payload)
    return NextResponse.json(asProductAdmin(created), { status: 201 })
  } catch (error) {
    const { status, body } = resolveInventoryError(error, "Unable to create product in inventory service.")
    return NextResponse.json(body, { status })
  }
}

function normalizeExpiryDate(value: unknown): string | undefined {
  if (typeof value !== "string") return undefined
  const trimmed = value.trim()
  if (!trimmed) return undefined

  const date = new Date(trimmed.endsWith("Z") || /[+-]\d{2}:\d{2}$/.test(trimmed) ? trimmed : `${trimmed}Z`)
  if (Number.isNaN(date.getTime())) return undefined
  const iso = date.toISOString()
  return `${iso.slice(0, 19)}Z`
}
