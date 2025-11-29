import { NextResponse } from "next/server"
import type { CartLine } from "@/lib/types"
import { calculateCartTotals } from "@/lib/cart-utils"

export async function POST(request: Request) {
  try {
    const body = await request.json()
    const {
      lines,
      tip = 0,
      discount = 0,
    } = body as {
      lines: CartLine[]
      tip?: number
      discount?: number
    }

    const totals = calculateCartTotals(lines, tip, discount)

    return NextResponse.json(totals)
  } catch (error) {
    return NextResponse.json({ error: "Failed to calculate price" }, { status: 500 })
  }
}
