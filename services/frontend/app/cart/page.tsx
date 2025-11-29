"use client"

import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Separator } from "@/components/ui/separator"
import { ArrowLeft, Minus, Plus, ShoppingCart, Trash2 } from "lucide-react"
import { useCartStore } from "@/lib/store"

export default function CartPage() {
  const router = useRouter()
  const { lines, updateLineQty, removeLine, getTotals } = useCartStore()
  const totals = getTotals()

  const handleCheckout = () => {
    router.push("/checkout")
  }

  const handleContinueShopping = () => {
    router.push("/menu")
  }

  if (lines.length === 0) {
    return (
      <div className="kiosk-mode min-h-screen bg-background flex flex-col">
        <header className="bg-card border-b-4 border-border shadow-lg">
          <div className="container mx-auto px-8 py-6 flex items-center gap-6">
            <Button
              variant="outline"
              size="lg"
              onClick={handleContinueShopping}
              className="touch-target text-xl px-6 py-6 bg-transparent"
            >
              <ArrowLeft className="w-6 h-6 mr-2" />
              Back to Menu
            </Button>
            <div>
              <h1 className="text-4xl font-bold text-foreground">Your Cart</h1>
            </div>
          </div>
        </header>

        <div className="flex-1 flex items-center justify-center">
          <div className="text-center space-y-6 max-w-md">
            <div className="w-32 h-32 mx-auto bg-muted rounded-full flex items-center justify-center">
              <ShoppingCart className="w-16 h-16 text-muted-foreground" />
            </div>
            <h2 className="text-4xl font-bold text-foreground">Your cart is empty</h2>
            <p className="text-2xl text-muted-foreground">Add some delicious items to get started!</p>
            <Button
              size="lg"
              onClick={handleContinueShopping}
              className="touch-target text-2xl px-12 py-8 bg-accent hover:bg-accent/90 text-accent-foreground"
            >
              Browse Menu
            </Button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="kiosk-mode min-h-screen bg-background flex flex-col">
      <header className="bg-card border-b-4 border-border shadow-lg">
        <div className="container mx-auto px-8 py-6 flex items-center justify-between">
          <div className="flex items-center gap-6">
            <Button
              variant="outline"
              size="lg"
              onClick={handleContinueShopping}
              className="touch-target text-xl px-6 py-6 bg-transparent"
            >
              <ArrowLeft className="w-6 h-6 mr-2" />
              Back
            </Button>
            <div>
              <h1 className="text-4xl font-bold text-foreground">Your Cart</h1>
              <p className="text-xl text-muted-foreground mt-1">
                {lines.reduce((sum, line) => sum + line.qty, 0)} items
              </p>
            </div>
          </div>
        </div>
      </header>

      <div className="flex-1 flex">
        <main className="flex-1 p-8">
          <ScrollArea className="h-full">
            <div className="max-w-4xl mx-auto space-y-6 pb-8">
              {lines.map((line) => (
                <Card key={line.lineId} className="border-2 overflow-hidden">
                  <CardContent className="p-6">
                    <div className="flex gap-6">
                      <div className="flex-1 space-y-4">
                        <div className="flex items-start justify-between gap-4">
                          <div>
                            <h3 className="text-3xl font-bold text-foreground">{line.name}</h3>
                            <p className="text-xl text-muted-foreground mt-1">${line.unitPrice.toFixed(2)} each</p>
                          </div>
                          <Button
                            variant="ghost"
                            size="lg"
                            onClick={() => removeLine(line.lineId)}
                            className="touch-target text-destructive hover:text-destructive hover:bg-destructive/10"
                          >
                            <Trash2 className="w-6 h-6" />
                          </Button>
                        </div>

                        <Separator />

                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-4">
                            <Button
                              variant="outline"
                              size="lg"
                              onClick={() => updateLineQty(line.lineId, line.qty - 1)}
                              className="touch-target w-14 h-14 text-xl"
                            >
                              <Minus className="w-5 h-5" />
                            </Button>
                            <span className="text-3xl font-bold text-foreground w-16 text-center">{line.qty}</span>
                            <Button
                              variant="outline"
                              size="lg"
                              onClick={() => updateLineQty(line.lineId, line.qty + 1)}
                              className="touch-target w-14 h-14 text-xl"
                            >
                              <Plus className="w-5 h-5" />
                            </Button>
                          </div>
                          <div className="text-right">
                            <p className="text-xl text-muted-foreground">Line Total</p>
                            <p className="text-3xl font-bold text-accent">${line.lineTotal.toFixed(2)}</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </ScrollArea>
        </main>

        <aside className="w-96 bg-card border-l-4 border-border shadow-2xl flex flex-col">
          <div className="p-8 space-y-6 flex-1">
            <h3 className="text-3xl font-bold text-foreground">Order Summary</h3>
            <Separator />

            <div className="space-y-4">
              <h4 className="text-2xl font-semibold text-foreground">Items</h4>
              <div className="space-y-3">
                {lines.map((line) => {
                  const basePrice = line.baseUnitPrice ?? line.unitPrice
                  const discountPerUnit = Math.max(0, basePrice - line.unitPrice)
                  const totalDiscount = discountPerUnit * line.qty
                  return (
                    <div key={line.lineId} className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-lg font-semibold text-foreground">
                          {line.qty}× {line.name}
                        </p>
                        <p className="text-sm text-muted-foreground">Unit: ${line.unitPrice.toFixed(2)}</p>
                        {discountPerUnit > 0 && (
                          <p className="text-sm text-green-600">
                            {line.promoName ? `${line.promoName}: ` : "Discount: "}
                            -${totalDiscount.toFixed(2)} ({discountPerUnit.toFixed(2)}/ea)
                          </p>
                        )}
                      </div>
                      <p className="text-lg font-semibold text-foreground">${line.lineTotal.toFixed(2)}</p>
                    </div>
                  )
                })}
              </div>
            </div>

            <Separator />

            <div className="space-y-4">
              <div className="flex justify-between text-xl">
                <span className="text-muted-foreground">Subtotal</span>
                <span className="font-semibold">${totals.subtotal.toFixed(2)}</span>
              </div>
              {totals.discounts > 0 && (
                <div className="flex justify-between text-xl text-green-600">
                  <span>Discounts</span>
                  <span>-${totals.discounts.toFixed(2)}</span>
                </div>
              )}
            </div>

            <Separator />

            <div className="flex justify-between items-center">
              <span className="text-2xl font-bold text-foreground">Total</span>
              <span className="text-4xl font-bold text-accent">${totals.total.toFixed(2)}</span>
            </div>

            <div className="text-center text-lg text-muted-foreground">
              {lines.reduce((sum, line) => sum + line.qty, 0)} items in cart
            </div>
          </div>

          <div className="p-8 border-t-4 border-border space-y-4">
            <Button
              size="lg"
              onClick={handleCheckout}
              className="touch-target w-full text-2xl py-8 bg-accent hover:bg-accent/90 text-accent-foreground"
            >
              Proceed to Checkout
            </Button>
            <Button
              variant="outline"
              size="lg"
              onClick={handleContinueShopping}
              className="touch-target w-full text-xl py-6 bg-transparent"
            >
              Add More Items
            </Button>
          </div>
        </aside>
      </div>
    </div>
  )
}
