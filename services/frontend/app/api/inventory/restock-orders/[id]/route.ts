import { NextResponse } from "next/server"

// Mock restock orders store
const restockOrders: any[] = []

export async function PATCH(request: Request, { params }: { params: { id: string } }) {
  const { id } = params
  const body = await request.json()

  const order = restockOrders.find((o) => o.id === id)
  if (!order) {
    return NextResponse.json({ error: "Order not found" }, { status: 404 })
  }

  if (body.status) {
    order.status = body.status
  }

  return NextResponse.json(order)
}
