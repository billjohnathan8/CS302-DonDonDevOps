import { NextResponse } from "next/server"
import type { RestockOrder } from "@/lib/types"

// In-memory store for demo
const restockOrders: RestockOrder[] = []

export async function GET() {
  return NextResponse.json(restockOrders)
}

export async function POST(request: Request) {
  const body = await request.json()
  const newOrder: RestockOrder = {
    id: `RO-${Date.now()}`,
    supplier: body.supplier,
    items: body.items,
    status: "draft",
    createdAt: new Date().toISOString(),
    expectedDate: body.expectedDate,
  }
  restockOrders.push(newOrder)
  return NextResponse.json(newOrder)
}
