import { NextResponse } from "next/server"

export async function GET(request: Request, { params }: { params: { id: string } }) {
  const { id } = params

  // Mock receipt data
  const receipt = {
    orderId: id,
    receiptUrl: `https://example.com/receipts/${id}`,
    qrCode: `ORDER-${id}`,
  }

  return NextResponse.json(receipt)
}
