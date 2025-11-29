import type { Promotion } from "@/lib/types"

const PROMOTIONS_SERVICE_BASE_URL = resolveServiceBaseUrl(
  process.env.PROMOTIONS_SERVICE_URL,
  "http://localhost:8082",
  "/promotions",
)

export type PromotionRecord = {
  id?: string
  name: string
  startTime: string
  endTime: string
  discountRate: number
}

export type PromotionCreatePayload = {
  name: string
  startTime: string
  endTime: string
  discountRate: number
}

export type PromotionPatchPayload = Partial<PromotionCreatePayload>

export type ApplyItemPayload = {
  productId: string
  quantity: number
  unitPrice: number
}

export type ApplyRequestPayload = {
  now?: string
  items: ApplyItemPayload[]
}

export type ApplyResponseItem = {
  productId: string
  discountRate: number
  discountAmount: number
  finalUnitPrice: number
}

export type ApplyResponsePayload = {
  items: ApplyResponseItem[]
}

class PromotionsServiceError extends Error {
  status: number
  body?: unknown

  constructor(status: number, message: string, body?: unknown) {
    super(message)
    this.name = "PromotionsServiceError"
    this.status = status
    this.body = body
  }
}

export function resolvePromotionsError(error: unknown, fallbackMessage: string) {
  if (error instanceof PromotionsServiceError) {
    return {
      status: error.status,
      body: error.body ?? { error: "PromotionsError", message: error.message },
    }
  }

  return {
    status: 502,
    body: { error: "PromotionsUnavailable", message: fallbackMessage },
  }
}

export function asPromotion(record: PromotionRecord): Promotion {
  const fallbackId = `${record.name}-${record.startTime}`
  return {
    promoId: record.id ?? fallbackId,
    name: record.name,
    discountRate: record.discountRate,
    startDate: record.startTime,
    endDate: record.endTime,
  }
}

export async function listPromotions() {
  return promotionsFetch<PromotionRecord[]>("", { method: "GET" })
}

export async function createPromotion(payload: PromotionCreatePayload) {
  return promotionsFetch<PromotionRecord>("", { method: "POST", body: payload })
}

export async function patchPromotion(id: string, payload: PromotionPatchPayload) {
  return promotionsFetch<PromotionRecord>(`/${id}`, { method: "PATCH", body: payload })
}

export async function deletePromotion(id: string) {
  await promotionsFetch<void>(`/${id}`, { method: "DELETE" })
}

export async function applyPromotions(payload: ApplyRequestPayload) {
  return promotionsFetch<ApplyResponsePayload>("/apply", { method: "POST", body: payload })
}

type FetchOptions = {
  method?: "GET" | "POST" | "PATCH" | "DELETE" | "PUT"
  body?: unknown
}

async function promotionsFetch<T>(path: string, options: FetchOptions): Promise<T> {
  const base = PROMOTIONS_SERVICE_BASE_URL.replace(/\/+$/, "")
  const suffix = path ? (path.startsWith("/") ? path : `/${path}`) : ""
  const url = `${base}${suffix}`
  const response = await fetch(url, {
    method: options.method ?? "GET",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: options.body ? JSON.stringify(options.body) : undefined,
    cache: "no-store",
  })

  const text = await response.text()
  const parsedBody = safeParseJson(text)

  if (!response.ok) {
    throw new PromotionsServiceError(response.status, `Promotions request to ${suffix} failed`, parsedBody ?? text)
  }

  return (parsedBody as T) ?? (undefined as T)
}

function safeParseJson(body: string) {
  if (!body) return undefined
  try {
    return JSON.parse(body)
  } catch {
    return undefined
  }
}

export function normalizePromoDate(value: string | undefined) {
  if (!value) return undefined
  const trimmed = value.trim()
  if (!trimmed) return undefined
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(trimmed)) {
    const date = new Date(trimmed)
    if (Number.isNaN(date.getTime())) return undefined
    return date.toISOString()
  }
  if (/Z$/.test(trimmed)) {
    return trimmed
  }
  const date = new Date(trimmed)
  if (Number.isNaN(date.getTime())) {
    return undefined
  }
  return date.toISOString()
}

export function validatePromotionPayload(payload: PromotionCreatePayload) {
  if (payload.discountRate < 0.01 || payload.discountRate > 1) {
    throw new PromotionsServiceError(400, "Discount rate must be between 1% and 100%.")
  }

  const start = Date.parse(payload.startTime)
  const end = Date.parse(payload.endTime)
  if (Number.isNaN(start) || Number.isNaN(end) || end <= start) {
    throw new PromotionsServiceError(400, "End time must be after start time.")
  }
}

function resolveServiceBaseUrl(envValue: string | undefined, fallbackOrigin: string, resourcePath: string) {
  const candidate = envValue?.trim()
  const normalizedOrigin = (candidate && candidate.length > 0 ? candidate : fallbackOrigin).replace(/\/+$/, "")
  if (normalizedOrigin.endsWith(resourcePath)) {
    return normalizedOrigin
  }
  return `${normalizedOrigin}${resourcePath}`
}

export { PROMOTIONS_SERVICE_BASE_URL }
