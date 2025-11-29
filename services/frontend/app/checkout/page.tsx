"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Separator } from "@/components/ui/separator"
import { Label } from "@/components/ui/label"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { ArrowLeft, CreditCard, DollarSign, Loader2, CheckCircle2 } from "lucide-react"
import { useCartStore } from "@/lib/store"
import { CardForm } from "@/components/card-form"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog"

export default function CheckoutPage() {
  const router = useRouter()
  const { lines, getTotals, clearCart } = useCartStore()
  const [paymentMethod, setPaymentMethod] = useState<string>("card")
  const [processing, setProcessing] = useState(false)
  const [isCardValid, setIsCardValid] = useState(false)
  const [showPaymentComplete, setShowPaymentComplete] = useState(false)
  const [completedOrder, setCompletedOrder] = useState<{
    id: string
    items: typeof lines
    totals: ReturnType<typeof getTotals>
  } | null>(null)

  useEffect(() => {
    if (lines.length === 0) {
      router.push("/cart")
    }
  }, [lines.length, router])

  const finalTotals = getTotals()

  if (lines.length === 0) {
    return null
  }

  const handlePayment = async () => {
    if (paymentMethod === "cash") {
      alert("Cash payments are not supported yet. Please select card.")
      return
    }

    if (!isCardValid) {
      alert("Please complete all card details correctly")
      return
    }

    setProcessing(true)

    try {
      const response = await fetch("/api/orders", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          items: lines.map((line) => ({
            product_Id: line.productId,
            quantity: line.qty,
          })),
          payment_info: {
            payment_method_id: "pm_card_mastercard",
            currency: "sgd",
          },
        }),
      })

      const payload = await response.json()

      if (!response.ok) {
        const message = payload?.message || "Failed to create order"
        throw new Error(message)
      }

      setCompletedOrder({
        id: payload.id,
        items: [...lines],
        totals: { ...finalTotals },
      })
      setShowPaymentComplete(true)

      setTimeout(() => {
        clearCart()
        router.push(`/order/${payload.id}`)
      }, 5000)
    } catch (error) {
      console.error("[DonDonDevOps] Payment error:", error)
      alert(error instanceof Error ? error.message : "Payment failed. Please try again.")
    } finally {
      setProcessing(false)
    }
  }

  return (
    <div className="kiosk-mode min-h-screen bg-background flex flex-col">
      <Dialog open={showPaymentComplete} onOpenChange={setShowPaymentComplete}>
        <DialogContent className="max-w-3xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <div className="flex flex-col items-center space-y-4 mb-4">
              <div className="w-20 h-20 rounded-full bg-green-100 flex items-center justify-center">
                <CheckCircle2 className="w-12 h-12 text-green-600" />
              </div>
              <DialogTitle className="text-4xl font-bold text-center">Payment Completed!</DialogTitle>
              <DialogDescription className="text-xl text-center">
                Your order has been successfully placed
              </DialogDescription>
            </div>
          </DialogHeader>

          {completedOrder && (
            <div className="space-y-6">
              <Separator />

              <div className="space-y-4">
                <h3 className="text-2xl font-semibold text-foreground">Purchased Items</h3>
                {completedOrder.items.map((item) => (
                  <div key={item.lineId} className="flex justify-between items-start py-2">
                    <div className="flex-1">
                      <p className="text-xl font-medium">
                        {item.qty}× {item.name}
                      </p>
                      <p className="text-lg text-muted-foreground">${item.unitPrice.toFixed(2)} each</p>
                    </div>
                    <p className="text-xl font-semibold">${item.lineTotal.toFixed(2)}</p>
                  </div>
                ))}
              </div>

              <Separator />

              <div className="space-y-3">
                <div className="flex justify-between text-xl">
                  <span className="text-muted-foreground">Subtotal</span>
                  <span className="font-semibold">${completedOrder.totals.subtotal.toFixed(2)}</span>
                </div>
                {completedOrder.totals.discounts > 0 && (
                  <div className="flex justify-between text-xl text-green-600">
                    <span>Discounts</span>
                    <span>-${completedOrder.totals.discounts.toFixed(2)}</span>
                  </div>
                )}
                <Separator />
                <div className="flex justify-between items-center">
                  <span className="text-2xl font-bold text-foreground">Total Paid</span>
                  <span className="text-3xl font-bold text-accent">${completedOrder.totals.total.toFixed(2)}</span>
                </div>
              </div>

              <div className="mt-6 p-4 bg-muted/30 rounded-lg text-center">
                <p className="text-lg text-muted-foreground">Redirecting to order confirmation in 5 seconds...</p>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      <header className="bg-card border-b-4 border-border shadow-lg">
        <div className="container mx-auto px-8 py-6 flex items-center gap-6">
          <Button
            variant="outline"
            size="lg"
            onClick={() => router.push("/cart")}
            disabled={processing}
            className="touch-target text-xl px-6 py-6 bg-transparent"
          >
            <ArrowLeft className="w-6 h-6 mr-2" />
            Back
          </Button>
          <div>
            <h1 className="text-4xl font-bold text-foreground">Checkout</h1>
            <p className="text-xl text-muted-foreground mt-1">Complete your order</p>
          </div>
        </div>
      </header>

      <div className="flex-1 flex">
        <main className="flex-1 p-8">
          <ScrollArea className="h-full">
            <div className="max-w-4xl mx-auto space-y-8 pb-8">
              <Card className="border-2">
                <CardHeader className="bg-muted/50">
                  <CardTitle className="text-3xl flex items-center gap-3">
                    <CreditCard className="w-8 h-8 text-accent" />
                    Payment Method
                  </CardTitle>
                </CardHeader>
                <CardContent className="p-6">
                  <RadioGroup value={paymentMethod} onValueChange={setPaymentMethod}>
                    <div className="space-y-4">
                      <div
                        className={`flex items-center justify-between p-6 rounded-lg border-2 transition-all cursor-pointer ${
                          paymentMethod === "card"
                            ? "border-accent bg-accent/10"
                            : "border-border hover:border-accent/50"
                        }`}
                        onClick={() => setPaymentMethod("card")}
                      >
                        <div className="flex items-center gap-4">
                          <RadioGroupItem value="card" id="payment-card" className="w-7 h-7" />
                          <Label htmlFor="payment-card" className="text-2xl font-semibold cursor-pointer">
                            Credit / Debit Card
                          </Label>
                        </div>
                        <CreditCard className="w-8 h-8 text-muted-foreground" />
                      </div>

                      <div
                        className={`flex items-center justify-between p-6 rounded-lg border-2 transition-all cursor-pointer ${
                          paymentMethod === "cash"
                            ? "border-accent bg-accent/10"
                            : "border-border hover:border-accent/50"
                        }`}
                        onClick={() => setPaymentMethod("cash")}
                      >
                        <div className="flex items-center gap-4">
                          <RadioGroupItem value="cash" id="payment-cash" className="w-7 h-7" />
                          <Label htmlFor="payment-cash" className="text-2xl font-semibold cursor-pointer">
                            Cash
                          </Label>
                        </div>
                        <DollarSign className="w-8 h-8 text-muted-foreground" />
                      </div>
                    </div>
                  </RadioGroup>

                  {paymentMethod === "card" && <CardForm onValidChange={setIsCardValid} />}

                  {paymentMethod === "cash" && (
                    <div className="mt-6 p-6 bg-muted/30 rounded-lg">
                      <p className="text-lg text-muted-foreground text-center">
                        Please have your cash ready. Staff will assist with payment.
                      </p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </ScrollArea>
        </main>

        <aside className="w-96 bg-card border-l-4 border-border shadow-2xl flex flex-col">
          <div className="p-8 space-y-6 flex-1">
            <h3 className="text-3xl font-bold text-foreground">Order Summary</h3>
            <Separator />

            <div className="space-y-3">
              <h4 className="text-xl font-semibold text-muted-foreground">Items</h4>
              {lines.map((line) => (
                <div key={line.lineId} className="flex justify-between text-lg">
                  <span className="text-foreground">
                    {line.qty}× {line.name}
                  </span>
                  <span className="font-semibold">${line.lineTotal.toFixed(2)}</span>
                </div>
              ))}
            </div>

            <Separator />

            <div className="space-y-4">
              <div className="flex justify-between text-xl">
                <span className="text-muted-foreground">Subtotal</span>
                <span className="font-semibold">${finalTotals.subtotal.toFixed(2)}</span>
              </div>
              {finalTotals.discounts > 0 && (
                <div className="flex justify-between text-xl text-green-600">
                  <span>Discounts</span>
                  <span>-${finalTotals.discounts.toFixed(2)}</span>
                </div>
              )}
            </div>

            <Separator />

            <div className="flex justify-between items-center">
              <span className="text-2xl font-bold text-foreground">Total</span>
              <span className="text-4xl font-bold text-accent">${finalTotals.total.toFixed(2)}</span>
            </div>
          </div>

          <div className="p-8 border-t-4 border-border">
            <Button
              size="lg"
              onClick={handlePayment}
              disabled={processing || (paymentMethod === "card" && !isCardValid)}
              className="touch-target w-full text-2xl py-8 bg-accent hover:bg-accent/90 text-accent-foreground disabled:opacity-50"
            >
              {processing ? (
                <>
                  <Loader2 className="w-6 h-6 mr-3 animate-spin" />
                  Processing...
                </>
              ) : (
                <>
                  <CreditCard className="w-6 h-6 mr-3" />
                  Pay ${finalTotals.total.toFixed(2)}
                </>
              )}
            </Button>
          </div>
        </aside>
      </div>
    </div>
  )
}
