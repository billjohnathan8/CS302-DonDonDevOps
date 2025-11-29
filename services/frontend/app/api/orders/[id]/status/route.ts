import { NextResponse } from "next/server"
import type { OrderStatus } from "@/lib/types"

// In-memory store for demo
const orderStatuses = new Map<string, OrderStatus>()

export async function GET(request: Request, { params }: { params: { id: string } }) {
  const status = orderStatuses.get(params.id) || "placed"
  return NextResponse.json({ status })
}

export async function POST(request: Request, { params }: { params: { id: string } }) {
  try {
    const body = await request.json()
    const { status } = body as { status: OrderStatus }

    orderStatuses.set(params.id, status)

    return NextResponse.json({ status })
  } catch (error) {
    return NextResponse.json({ error: "Failed to update order status" }, { status: 500 })
  }
}
