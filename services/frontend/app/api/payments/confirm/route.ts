import { NextResponse } from "next/server"
import type { PaymentConfirmResponse } from "@/lib/types"

export async function POST(request: Request) {
  try {
    const body = await request.json()
    const { orderId } = body as { orderId: string }

    // Simulate payment success
    const response: PaymentConfirmResponse = {
      success: true,
      orderId,
    }

    return NextResponse.json(response)
  } catch (error) {
    return NextResponse.json({ error: "Payment failed" }, { status: 500 })
  }
}
