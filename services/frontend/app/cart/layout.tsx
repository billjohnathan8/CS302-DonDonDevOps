import type React from "react"
import type { Metadata } from "next"

export const metadata: Metadata = {
  title: "Cart - Kiosk",
  description: "Review your order",
}

export default function CartLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return children
}
