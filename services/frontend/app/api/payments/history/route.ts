const PAYMENTS_SERVICE_BASE_URL = resolveBaseUrl(process.env.PAYMENTS_SERVICE_URL, "http://localhost:8083", "/payments")

export async function GET() {
  try {
    const response = await fetch(PAYMENTS_SERVICE_BASE_URL, {
      method: "GET",
      headers: { Accept: "application/json" },
      cache: "no-store",
    })

    if (!response.ok) {
      const body = await response.text()
      return new Response(body || "Unable to load payments", { status: response.status })
    }

    const data = await response.json()
    return Response.json(data)
  } catch (error) {
    console.error("[DonDonDevOps] Failed to load payments:", error)
    return Response.json(
      { error: "PaymentsUnavailable", message: "Unable to load payment history." },
      { status: 502 },
    )
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
