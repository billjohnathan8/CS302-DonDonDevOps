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
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { CalendarIcon } from "lucide-react"
import { format } from "date-fns"

type RestockDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  productName: string
  onSubmit: (data: { qty: number; expiryDate: string }) => void
}

export function RestockDialog({ open, onOpenChange, productName, onSubmit }: RestockDialogProps) {
  const [qty, setQty] = useState("")
  const [expiryDate, setExpiryDate] = useState<Date>()

  const handleSubmit = () => {
    if (!qty || !expiryDate) return

    onSubmit({
      qty: Number.parseInt(qty),
      expiryDate: expiryDate.toISOString(),
    })

    // Reset form
    setQty("")
    setExpiryDate(undefined)
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle className="text-2xl">Restock Order</DialogTitle>
          <DialogDescription className="text-lg">{productName}</DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="qty" className="text-lg">
              Quantity
            </Label>
            <Input
              id="qty"
              type="number"
              min="1"
              value={qty}
              onChange={(e) => setQty(e.target.value)}
              placeholder="Enter quantity"
              className="text-lg h-12"
            />
          </div>

          <div className="space-y-2">
            <Label className="text-lg">Expiry Date</Label>
            <Popover>
              <PopoverTrigger asChild>
                <Button
                  variant="outline"
                  size="lg"
                  className="w-full justify-start text-left font-normal text-lg bg-transparent"
                >
                  <CalendarIcon className="mr-2 h-5 w-5" />
                  {expiryDate ? format(expiryDate, "PPP") : <span>Pick a date</span>}
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-auto p-0">
                <Calendar mode="single" selected={expiryDate} onSelect={setExpiryDate} initialFocus />
              </PopoverContent>
            </Popover>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" size="lg" onClick={() => onOpenChange(false)} className="text-lg px-6 py-6">
            Cancel
          </Button>
          <Button
            size="lg"
            onClick={handleSubmit}
            disabled={!qty || !expiryDate}
            className="text-lg px-6 py-6 bg-accent hover:bg-accent/90"
          >
            Create Order
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
