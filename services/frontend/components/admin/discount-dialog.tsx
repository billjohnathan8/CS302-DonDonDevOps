"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Separator } from "@/components/ui/separator"

type DiscountDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  productName: string
  currentPrice: number
  onApply: (discountRate: number) => void
}

export function DiscountDialog({ open, onOpenChange, productName, currentPrice, onApply }: DiscountDialogProps) {
  const [discountPercent, setDiscountPercent] = useState("")

  const discountRate = Number.parseFloat(discountPercent) / 100 || 0
  const newPrice = currentPrice * (1 - discountRate)

  const handleApply = () => {
    if (!discountPercent || discountRate <= 0 || discountRate >= 1) return

    onApply(discountRate)
    setDiscountPercent("")
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle className="text-2xl">Set Discount</DialogTitle>
          <DialogDescription className="text-lg">{productName}</DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="discount" className="text-lg">
              Discount Percentage
            </Label>
            <Input
              id="discount"
              type="number"
              min="1"
              max="99"
              value={discountPercent}
              onChange={(e) => setDiscountPercent(e.target.value)}
              placeholder="Enter discount %"
              className="text-lg h-12"
            />
          </div>

          <Separator />

          <div className="space-y-3 bg-muted p-4 rounded-lg">
            <h3 className="text-lg font-semibold">Price Preview</h3>
            <div className="flex justify-between items-center">
              <span className="text-base text-muted-foreground">Original Price:</span>
              <span className="text-xl font-semibold line-through">${currentPrice.toFixed(2)}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-base text-muted-foreground">Discount:</span>
              <span className="text-xl font-semibold text-destructive">-{discountPercent || 0}%</span>
            </div>
            <Separator />
            <div className="flex justify-between items-center">
              <span className="text-lg font-bold">New Price:</span>
              <span className="text-2xl font-bold text-accent">${newPrice.toFixed(2)}</span>
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" size="lg" onClick={() => onOpenChange(false)} className="text-lg px-6 py-6">
            Cancel
          </Button>
          <Button
            size="lg"
            onClick={handleApply}
            disabled={!discountPercent || discountRate <= 0 || discountRate >= 1}
            className="text-lg px-6 py-6 bg-accent hover:bg-accent/90"
          >
            Apply Discount
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
