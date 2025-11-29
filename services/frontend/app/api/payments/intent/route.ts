import { NextResponse } from "next/server"
import type { PaymentIntentResponse } from "@/lib/types"

export async function POST() {
  // Simulated payment intent
  const response: PaymentIntentResponse = {
    simulated: true,
  }

  return NextResponse.json(response)
}
