import { NextResponse } from "next/server"
import {
  patchPromotion,
  deletePromotion,
  asPromotion,
  normalizePromoDate,
  resolvePromotionsError,
} from "@/lib/promotions-service"

export async function PATCH(request: Request, { params }: { params: { id: string } }) {
  try {
    if (!params?.id) {
      return NextResponse.json({ error: "InvalidPromotion", message: "Promotion ID is required." }, { status: 400 })
    }

    const body = await request.json()
    const payload: Record<string, unknown> = {}

    if (typeof body.name === "string") payload.name = body.name

    if (typeof body.discountRate === "number") {
      payload.discountRate = body.discountRate
    } else if (typeof body.discountPercent === "number") {
      payload.discountRate = body.discountPercent / 100
    }

    if (typeof body.startDate === "string" || typeof body.startTime === "string") {
      const normalized = normalizePromoDate(body.startDate ?? body.startTime)
      if (normalized) payload.startTime = normalized
    }

    if (typeof body.endDate === "string" || typeof body.endTime === "string") {
      const normalized = normalizePromoDate(body.endDate ?? body.endTime)
      if (normalized) payload.endTime = normalized
    }

    const updated = await patchPromotion(params.id, payload)
    return NextResponse.json(asPromotion(updated))
  } catch (error) {
    const { status, body } = resolvePromotionsError(error, "Unable to update promotion.")
    return NextResponse.json(body, { status })
  }
}

export async function DELETE(request: Request, { params }: { params: { id: string } }) {
  try {
    if (!params?.id) {
      return NextResponse.json({ error: "InvalidPromotion", message: "Promotion ID is required." }, { status: 400 })
    }
    await deletePromotion(params.id)
    return new Response(null, { status: 204 })
  } catch (error) {
    const { status, body } = resolvePromotionsError(error, "Unable to delete promotion.")
    return NextResponse.json(body, { status })
  }
}
