import { NextResponse } from "next/server"
import {
  asPromotion,
  createPromotion,
  listPromotions,
  normalizePromoDate,
  resolvePromotionsError,
  validatePromotionPayload,
} from "@/lib/promotions-service"

export async function GET() {
  try {
    const promos = await listPromotions()
    return NextResponse.json(promos.map(asPromotion))
  } catch (error) {
    const { status, body } = resolvePromotionsError(error, "Unable to load promotions.")
    return NextResponse.json(body, { status })
  }
}

export async function POST(request: Request) {
  try {
    const body = await request.json()
    const startTime = normalizePromoDate(body.startDate ?? body.startTime)
    const endTime = normalizePromoDate(body.endDate ?? body.endTime)
    const discountRate = normalizeDiscountRate(body.discountRate, body.discountPercent)

    if (!body.name || !startTime || !endTime || discountRate === undefined) {
      return NextResponse.json({ error: "InvalidRequest", message: "Missing required promotion fields." }, { status: 400 })
    }

    const payload = {
      name: body.name,
      startTime,
      endTime,
      discountRate,
    }

    validatePromotionPayload(payload)

    const created = await createPromotion(payload)
    return NextResponse.json(asPromotion(created), { status: 201 })
  } catch (error) {
    const { status, body } = resolvePromotionsError(error, "Unable to create promotion.")
    return NextResponse.json(body, { status })
  }
}

function normalizeDiscountRate(rateValue?: unknown, percentValue?: unknown) {
  if (typeof rateValue === "number") {
    return rateValue
  }
  if (typeof rateValue === "string" && rateValue.trim() !== "") {
    const parsed = Number(rateValue)
    return Number.isFinite(parsed) ? parsed : undefined
  }
  if (typeof percentValue === "number") {
    return percentValue / 100
  }
  if (typeof percentValue === "string" && percentValue.trim() !== "") {
    const parsed = Number(percentValue)
    return Number.isFinite(parsed) ? parsed / 100 : undefined
  }
  return undefined
}
