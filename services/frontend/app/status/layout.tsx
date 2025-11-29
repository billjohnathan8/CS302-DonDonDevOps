import type React from "react"
import type { Metadata } from "next"

export const metadata: Metadata = {
  title: "Order Status - Kiosk",
  description: "Track your order status",
}

export default function StatusLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return children
}
