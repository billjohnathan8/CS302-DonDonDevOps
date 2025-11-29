import type React from "react"
import type { Metadata } from "next"

export const metadata: Metadata = {
  title: "Menu - Kiosk",
  description: "Browse our menu and place your order",
}

export default function MenuLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return children
}
