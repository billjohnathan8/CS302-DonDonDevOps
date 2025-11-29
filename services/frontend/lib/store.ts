"use client"

import { create } from "zustand"
import type { CartLine } from "./types"
import { calculateLineTotal, calculateCartTotals, generateLineId } from "./cart-utils"

type CartStore = {
  lines: CartLine[]
  discountAmount: number
  addLine: (line: Omit<CartLine, "lineId" | "lineTotal">) => void
  updateLineQty: (lineId: string, qty: number) => void
  removeLine: (lineId: string) => void
  setDiscount: (amount: number) => void
  clearCart: () => void
  getTotals: () => ReturnType<typeof calculateCartTotals>
}

export const useCartStore = create<CartStore>((set, get) => ({
  lines: [],
  discountAmount: 0,

  addLine: (line) => {
    const existingLine = get().lines.find((l) => l.productId === line.productId)
    if (existingLine) {
      get().updateLineQty(existingLine.lineId, existingLine.qty + line.qty)
      return
    }

    const newLine: CartLine = {
      ...line,
      lineId: generateLineId(),
      lineTotal: 0,
    }
    newLine.lineTotal = calculateLineTotal(newLine)
    set((state) => ({ lines: [...state.lines, newLine] }))
  },

  updateLineQty: (lineId, qty) => {
    if (qty <= 0) {
      get().removeLine(lineId)
      return
    }
    set((state) => ({
      lines: state.lines.map((line) => {
        if (line.lineId === lineId) {
          const updated = { ...line, qty }
          updated.lineTotal = calculateLineTotal(updated)
          return updated
        }
        return line
      }),
    }))
  },

  removeLine: (lineId) => {
    set((state) => ({
      lines: state.lines.filter((line) => line.lineId !== lineId),
    }))
  },

  setDiscount: (amount) => set({ discountAmount: amount }),

  clearCart: () => set({ lines: [], discountAmount: 0 }),

  getTotals: () => {
    const { lines, discountAmount } = get()
    return calculateCartTotals(lines, discountAmount)
  },
}))
