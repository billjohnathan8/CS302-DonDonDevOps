import { NextResponse } from "next/server"

export async function POST(request: Request, { params }: { params: { id: string } }) {
  const { id } = params

  // Mock: Send receipt via email/SMS
  return NextResponse.json({
    success: true,
    receiptId: id,
    message: "Receipt sent successfully",
  })
}
