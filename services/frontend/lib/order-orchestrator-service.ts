const ORDER_ORCHESTRATOR_BASE_URL = resolveBaseUrl(
  process.env.ORDER_ORCHESTRATOR_URL,
  "http://localhost:8084",
  "/orders",
)

type FetchOptions = {
  method?: "GET" | "POST" | "PATCH" | "PUT" | "DELETE"
  body?: unknown
}

export type OrderOrchestratorCartItem = {
  product_id: string
  quantity: number
}

export type OrderOrchestratorPaymentInfo = {
  payment_method_id: string
  currency?: string
}

export type OrderOrchestratorPayload = {
  items: OrderOrchestratorCartItem[]
  payment_info: OrderOrchestratorPaymentInfo
}

export type OrderOrchestratorResponse = {
  order?: unknown
  clientSecret?: string
  [key: string]: unknown
}

export class OrderOrchestratorServiceError extends Error {
  status: number
  body?: unknown

  constructor(status: number, message: string, body?: unknown) {
    super(message)
    this.name = "OrderOrchestratorServiceError"
    this.status = status
    this.body = body
  }
}

export function resolveOrderOrchestratorError(error: unknown, fallbackMessage: string) {
  if (error instanceof OrderOrchestratorServiceError) {
    return {
      status: error.status,
      body: error.body ?? { error: "OrderOrchestratorError", message: error.message },
    }
  }

  return {
    status: 502,
    body: { error: "OrderOrchestratorUnavailable", message: fallbackMessage },
  }
}

export async function createOrchestratedOrder(payload: OrderOrchestratorPayload) {
  return orchestratorFetch<OrderOrchestratorResponse>("", {
    method: "POST",
    body: payload,
  })
}

async function orchestratorFetch<T>(path: string, options: FetchOptions): Promise<T> {
  const base = ORDER_ORCHESTRATOR_BASE_URL.replace(/\/+$/, "")
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
    throw new OrderOrchestratorServiceError(
      response.status,
      `Order orchestrator request to ${suffix || "/"} failed`,
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

function resolveBaseUrl(envValue: string | undefined, fallbackOrigin: string, path: string) {
  const candidate = envValue?.trim()
  const normalizedOrigin = (candidate && candidate.length > 0 ? candidate : fallbackOrigin).replace(/\/+$/, "")
  if (normalizedOrigin.endsWith(path)) {
    return normalizedOrigin
  }
  return `${normalizedOrigin}${path}`
}
