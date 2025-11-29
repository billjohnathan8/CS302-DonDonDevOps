import { NextResponse } from "next/server"
import type { UpdateProductRequest } from "@/lib/inventory-service"
import {
  asProductAdmin,
  deleteInventoryProduct,
  getInventoryProduct,
  resolveInventoryError,
  updateInventoryProduct,
} from "@/lib/inventory-service"

export async function GET(request: Request, { params }: { params: { id: string } }) {
  try {
    const product = await getInventoryProduct(params.id)
    return NextResponse.json(asProductAdmin(product))
  } catch (error) {
    const { status, body } = resolveInventoryError(error, "Unable to load product from inventory service.")
    return NextResponse.json(body, { status })
  }
}

export async function PATCH(request: Request, { params }: { params: { id: string } }) {
  try {
    const body = await request.json()
    const payload: UpdateProductRequest = {}

    if (typeof body.name === "string") payload.name = body.name
    if (typeof body.brand !== "undefined") payload.brand = body.brand ?? null
    if (typeof body.category !== "undefined") payload.category = body.category ?? null

    const priceCandidate = Number(body.priceSGD ?? body.price)
    if (Number.isFinite(priceCandidate)) payload.price = priceCandidate

    if (typeof body.expiryDate !== "undefined") {
      if (body.expiryDate === null || body.expiryDate === "") {
        payload.expiryDate = null
      } else {
        payload.expiryDate = normalizeExpiryDate(body.expiryDate) ?? null
      }
    }

    const updated = await updateInventoryProduct(params.id, payload)
    return NextResponse.json(asProductAdmin(updated))
  } catch (error) {
    const { status, body } = resolveInventoryError(error, "Unable to update product in inventory service.")
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


export async function DELETE(request: Request, { params }: { params: { id: string } }) {
  try {
    await deleteInventoryProduct(params.id)
    return new Response(null, { status: 204 })
  } catch (error) {
    const { status, body } = resolveInventoryError(error, "Unable to delete product from inventory service.")
    return NextResponse.json(body, { status })
  }
}
