import type React from "react"
import type { Metadata } from "next"

export const metadata: Metadata = {
  title: "Checkout - Kiosk",
  description: "Complete your payment",
}

export default function CheckoutLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return children
}
