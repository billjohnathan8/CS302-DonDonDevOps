import type { CartLine, CartTotals } from "./types"

export function calculateLineTotal(line: CartLine): number {
  return line.unitPrice * line.qty
}

export function calculateCartTotals(lines: CartLine[], discountAmount = 0): CartTotals {
  const subtotal = lines.reduce((sum, line) => sum + line.lineTotal, 0)
  const total = subtotal - discountAmount

  return {
    subtotal,
    discounts: discountAmount,
    total,
  }
}

export function generateLineId(): string {
  return `line_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

export function formatPrice(amount: number): string {
  return `$${amount.toFixed(2)}`
}

export function generateOrderNumber(): string {
  const letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
  const letter = letters[Math.floor(Math.random() * letters.length)]
  const number = Math.floor(Math.random() * 900) + 100
  return `${letter}-${number}`
}
