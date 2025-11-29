const PRODUCT_PROMOTION_SERVICE_BASE_URL = resolveBaseUrl()

type FetchOptions = {
  method?: "GET" | "POST"
  body?: unknown
}

export type ProductPromotionRecord = {
  id?: string
  promotionId?: string
  productId: string
}

export class ProductPromotionServiceError extends Error {
  status: number
  body?: unknown

  constructor(status: number, message: string, body?: unknown) {
    super(message)
    this.name = "ProductPromotionServiceError"
    this.status = status
    this.body = body
  }
}

export function resolveProductPromotionError(error: unknown, fallbackMessage: string) {
  if (error instanceof ProductPromotionServiceError) {
    return {
      status: error.status,
      body: error.body ?? { error: "ProductPromotionError", message: error.message },
    }
  }

  return {
    status: 502,
    body: { error: "ProductPromotionUnavailable", message: fallbackMessage },
  }
}

export async function listProductsForPromotion(promotionId: string) {
  const trimmed = promotionId.trim()
  if (!trimmed) {
    throw new ProductPromotionServiceError(400, "Promotion ID is required")
  }
  return productPromotionFetch<ProductPromotionRecord[]>(`/promotion/${trimmed}`, { method: "GET" })
}

export async function linkProductToPromotion(promotionId: string, productId: string) {
  if (!promotionId || !productId) {
    throw new ProductPromotionServiceError(400, "promotionId and productId are required")
  }
  return productPromotionFetch<ProductPromotionRecord>("", {
    method: "POST",
    body: { promotionId, productId },
  })
}

async function productPromotionFetch<T>(path: string, options: FetchOptions): Promise<T> {
  const base = PRODUCT_PROMOTION_SERVICE_BASE_URL.replace(/\/+$/, "")
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
    throw new ProductPromotionServiceError(
      response.status,
      `Product promotion request to ${suffix} failed`,
      parsedBody ?? text,
    )
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

function resolveBaseUrl() {
  const explicit = process.env.PRODUCT_PROMOTION_SERVICE_URL?.trim()
  if (explicit) {
    return explicit.replace(/\/+$/, "")
  }

  const promotionsOriginCandidate = process.env.PROMOTIONS_SERVICE_URL?.trim()
  const promotionsOrigin = promotionsOriginCandidate && promotionsOriginCandidate.length > 0
    ? promotionsOriginCandidate
    : "http://localhost:8082"
  const normalizedOrigin = promotionsOrigin.replace(/\/+$/, "")
  const resourcePath = "/productpromotions"
  if (normalizedOrigin.endsWith(resourcePath)) {
    return normalizedOrigin
  }
  return `${normalizedOrigin}${resourcePath}`
}
