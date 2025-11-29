import type { Product, ProductAdmin } from "@/lib/types"

const INVENTORY_SERVICE_BASE_URL = resolveInventoryBaseUrl()
function resolveInventoryBaseUrl() {
  const candidate = process.env.INVENTORY_SERVICE_URL?.trim()
  return (candidate && candidate.length > 0 ? candidate : "http://localhost:8081").replace(/\/+$/, "")
}

const DEFAULT_TIMEOUT_MS = 8000
const BACKGROUND_TIMEOUT_MS = 10000
const READ_RETRY_ATTEMPTS = 2
const RETRY_BACKOFF_MS = [500, 1000]

export type InventoryProduct = {
  productID: string
  name: string
  brand: string | null
  category: string | null
  priceInSGD: number
  stock: number
  expiryDate: string | null
  createdAt: string
  updatedAt: string
}

export type NewProductRequest = {
  name: string
  brand: string
  category: string
  price: number
  stock: number
  expiryDate?: string
}

export type UpdateProductRequest = Partial<{
  name: string
  brand: string | null
  category: string | null
  price: number
  expiryDate: string | null
}>

export type RestockRequest = {
  productId: string
  quantity: number
  expiryDate: string
}

export type ReduceStockRequest = {
  productId?: string
  quantity: number
}

export type ProblemResponse = {
  error: string
  message: string
}

type InventoryFetchOptions = {
  method?: "GET" | "POST" | "PATCH" | "DELETE"
  body?: unknown
  timeoutMs?: number
  retry?: boolean
}

export class InventoryServiceError extends Error {
  status: number
  details?: ProblemResponse | Record<string, unknown> | string

  constructor(status: number, message: string, details?: ProblemResponse | Record<string, unknown> | string) {
    super(message)
    this.name = "InventoryServiceError"
    this.status = status
    this.details = details
  }
}

export function resolveInventoryError(error: unknown, fallbackMessage: string) {
  if (error instanceof InventoryServiceError) {
    if (error.details && typeof error.details === "object" && "error" in error.details && "message" in error.details) {
      return {
        status: error.status,
        body: error.details as ProblemResponse,
      }
    }
    return {
      status: error.status,
      body: {
        error: "InventoryServiceError",
        message: error.message,
      },
    }
  }

  return {
    status: 502,
    body: {
      error: "InventoryServiceUnavailable",
      message: fallbackMessage,
    },
  }
}

export function asProductAdmin(product: InventoryProduct): ProductAdmin {
  return {
    ProductID: product.productID,
    name: product.name,
    brand: product.brand ?? "",
    category: product.category ?? "",
    priceSGD: product.priceInSGD,
    stock: product.stock,
    expiryDate: product.expiryDate ?? undefined,
  }
}

export function asMenuProduct(product: InventoryProduct): Product {
  return {
    id: product.productID,
    name: product.name,
    brand: product.brand ?? undefined,
    basePrice: product.priceInSGD,
    categoryId: product.category ?? "uncategorized",
    stock: { qty: product.stock },
  }
}

export async function listInventoryProducts() {
  return inventoryFetch<InventoryProduct[]>("/api/product", { method: "GET", retry: true, timeoutMs: BACKGROUND_TIMEOUT_MS })
}

export async function getInventoryProduct(productId: string) {
  return inventoryFetch<InventoryProduct>(`/api/product/${productId}`, { method: "GET", retry: true })
}

export async function createInventoryProduct(payload: NewProductRequest) {
  return inventoryFetch<InventoryProduct>("/api/product", { method: "POST", body: payload })
}

export async function updateInventoryProduct(productId: string, payload: UpdateProductRequest) {
  return inventoryFetch<InventoryProduct>(`/api/product/${productId}`, { method: "PATCH", body: payload })
}

export async function deleteInventoryProduct(productId: string) {
  return inventoryFetch<void>(`/api/product/${productId}`, { method: "DELETE" })
}

export async function restockInventory(payload: RestockRequest) {
  return inventoryFetch<InventoryProduct[]>("/api/inventory/restock", { method: "POST", body: payload })
}

export async function reduceInventoryStock(productId: string, payload: ReduceStockRequest) {
  return inventoryFetch<InventoryProduct>(`/api/inventory/reduce-stock/${productId}`, { method: "POST", body: payload })
}

async function inventoryFetch<T>(path: string, options: InventoryFetchOptions = {}): Promise<T> {
  const url = new URL(path, INVENTORY_SERVICE_BASE_URL)
  const method = options.method ?? "GET"
  const timeoutMs = options.timeoutMs ?? (method === "GET" ? DEFAULT_TIMEOUT_MS : DEFAULT_TIMEOUT_MS)
  const shouldRetry = options.retry ?? method === "GET"
  const attempts = shouldRetry ? READ_RETRY_ATTEMPTS + 1 : 1

  let lastError: InventoryServiceError | null = null

  for (let attempt = 0; attempt < attempts; attempt++) {
    const controller = new AbortController()
    const timeout = setTimeout(() => controller.abort(), timeoutMs)

    try {
      const response = await fetch(url.toString(), {
        method,
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
        body: options.body ? JSON.stringify(options.body) : undefined,
        signal: controller.signal,
        cache: "no-store",
      })

      const rawBody = await response.text()

      if (!response.ok) {
        const parsed = parseProblemPayload(rawBody)
        throw new InventoryServiceError(
          response.status,
          parsed?.message ?? `Inventory request to ${path} failed with status ${response.status}`,
          parsed ?? rawBody,
        )
      }

      if (!rawBody) {
        return undefined as T
      }

      try {
        return JSON.parse(rawBody) as T
      } catch (parseError) {
        throw new InventoryServiceError(
          502,
          `Unable to parse inventory service response for ${path}`,
          { error: "ParseError", message: rawBody },
        )
      }
    } catch (error) {
      const normalized = normalizeInventoryError(error, path)
      lastError = normalized

      const isRetriable =
        shouldRetry && attempt < attempts - 1 && (normalized.status >= 500 || normalized.status === 504 || normalized.status === 502)

      if (!isRetriable) {
        throw normalized
      }

      const backoff = RETRY_BACKOFF_MS[attempt] ?? RETRY_BACKOFF_MS[RETRY_BACKOFF_MS.length - 1]
      await delay(backoff)
    } finally {
      clearTimeout(timeout)
    }
  }

  throw lastError ?? new InventoryServiceError(502, `Inventory request to ${path} failed`)
}

function normalizeInventoryError(error: unknown, path: string): InventoryServiceError {
  if (error instanceof InventoryServiceError) {
    return error
  }

  if (error instanceof Error && error.name === "AbortError") {
    return new InventoryServiceError(504, `Inventory request to ${path} timed out`, {
      error: "Timeout",
      message: "Inventory service did not respond within the allotted time.",
    })
  }

  if (error instanceof Error) {
    return new InventoryServiceError(502, `Inventory request to ${path} failed`, {
      error: error.name,
      message: error.message,
    })
  }

  return new InventoryServiceError(502, `Inventory request to ${path} failed`, {
    error: "UnknownError",
    message: String(error),
  })
}

function parseProblemPayload(body: string) {
  if (!body) return undefined
  try {
    return JSON.parse(body) as ProblemResponse
  } catch {
    return undefined
  }
}

function delay(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

export { INVENTORY_SERVICE_BASE_URL, BACKGROUND_TIMEOUT_MS }
