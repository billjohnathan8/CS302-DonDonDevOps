"use client"

import { use, useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { CheckCircle2, Clock, Home, Loader2 } from "lucide-react"
import type { Order } from "@/lib/types"
import QRCode from "react-qr-code"

type PageProps = {
  params: Promise<{ id: string }>
}

export default function OrderConfirmationPage({ params }: PageProps) {
  const { id } = use(params)
  const router = useRouter()
  const [order, setOrder] = useState<Order | null>(null)
  const [loading, setLoading] = useState(true)
  const [countdown, setCountdown] = useState(30)

  useEffect(() => {
    // Fetch order details
    fetch(`/api/orders/${id}`)
      .then((res) => res.json())
      .then((data: Order) => {
        setOrder(data)
        setLoading(false)
      })
      .catch((err) => {
        console.error("[DonDonDevOps] Failed to load order:", err)
        setLoading(false)
      })
  }, [id])

  useEffect(() => {
    // Poll for order status updates
    if (!order) return

    const interval = setInterval(() => {
      fetch(`/api/orders/${id}/status`)
        .then((res) => res.json())
        .then((data) => {
          setOrder((prev) => (prev ? { ...prev, status: data.status } : null))
        })
        .catch((err) => {
          console.error("[DonDonDevOps] Failed to fetch order status:", err)
        })
    }, 5000) // Poll every 5 seconds

    return () => clearInterval(interval)
  }, [id, order])

  useEffect(() => {
    // Countdown timer to return to home
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          router.push("/")
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => clearInterval(timer)
  }, [router])

  const handleNewOrder = () => {
    router.push("/")
  }

  if (loading) {
    return (
      <div className="kiosk-mode min-h-screen bg-background flex items-center justify-center">
        <div className="text-center space-y-4">
          <div className="w-16 h-16 border-4 border-accent border-t-transparent rounded-full animate-spin mx-auto" />
          <p className="text-2xl text-muted-foreground">Loading order...</p>
        </div>
      </div>
    )
  }

  if (!order) {
    return (
      <div className="kiosk-mode min-h-screen bg-background flex items-center justify-center">
        <div className="text-center space-y-4">
          <p className="text-2xl text-destructive">Order not found</p>
          <Button onClick={handleNewOrder} size="lg">
            Start New Order
          </Button>
        </div>
      </div>
    )
  }

  const statusConfig = {
    placed: {
      icon: CheckCircle2,
      label: "Order Placed",
      color: "text-blue-600",
      bgColor: "bg-blue-100",
      description: "Your order has been received",
    },
    preparing: {
      icon: Loader2,
      label: "Preparing",
      color: "text-accent",
      bgColor: "bg-accent/10",
      description: "We're making your order now",
    },
    ready: {
      icon: CheckCircle2,
      label: "Ready for Pickup",
      color: "text-green-600",
      bgColor: "bg-green-100",
      description: "Your order is ready!",
    },
    completed: {
      icon: CheckCircle2,
      label: "Completed",
      color: "text-green-600",
      bgColor: "bg-green-100",
      description: "Order completed",
    },
  }

  const currentStatus = statusConfig[order.status]
  const StatusIcon = currentStatus.icon

  return (
    <div className="kiosk-mode min-h-screen bg-gradient-to-br from-background via-background to-secondary flex flex-col items-center justify-center p-8">
      <div className="max-w-4xl w-full space-y-8">
        {/* Success Header */}
        <div className="text-center space-y-6">
          <div className={`w-32 h-32 mx-auto rounded-full ${currentStatus.bgColor} flex items-center justify-center`}>
            <StatusIcon
              className={`w-20 h-20 ${currentStatus.color} ${order.status === "preparing" ? "animate-spin" : ""}`}
            />
          </div>
          <div>
            <h1 className="text-6xl font-bold text-foreground mb-3">Thank You!</h1>
            <p className="text-3xl text-muted-foreground">{currentStatus.description}</p>
          </div>
        </div>

        {/* Order Details Card */}
        <Card className="border-4 border-border shadow-2xl">
          <CardContent className="p-8 space-y-6">
            {/* Order Number */}
            <div className="text-center space-y-3">
              <p className="text-2xl text-muted-foreground">Order Number</p>
              <p className="text-7xl font-bold text-accent">{order.number}</p>
              <Badge className={`${currentStatus.bgColor} ${currentStatus.color} text-xl px-6 py-2`}>
                {currentStatus.label}
              </Badge>
            </div>

            <Separator />

            {/* QR Code for Order Tracking */}
            <div className="flex flex-col items-center space-y-4">
              <p className="text-xl text-muted-foreground">Scan to track your order</p>
              <div className="bg-white p-6 rounded-lg">
                <QRCode value={order.qrToken} size={200} />
              </div>
            </div>

            <Separator />

            {/* Order Items */}
            <div className="space-y-4">
              <h3 className="text-2xl font-semibold text-foreground">Your Order</h3>
              {order.items.map((item) => (
                <div key={item.lineId} className="flex justify-between items-start">
                  <div className="flex-1">
                    <p className="text-xl font-medium text-foreground">
                      {item.qty}× {item.name}
                    </p>
                    {item.modifiers.length > 0 && (
                      <div className="ml-6 mt-1 space-y-1">
                        {item.modifiers.map((mod, idx) => (
                          <p key={idx} className="text-lg text-muted-foreground">
                            • {mod.label}
                          </p>
                        ))}
                      </div>
                    )}
                  </div>
                  <p className="text-xl font-semibold text-foreground">${item.lineTotal.toFixed(2)}</p>
                </div>
              ))}
            </div>

            <Separator />

            {/* Order Totals */}
            <div className="space-y-3">
              <div className="flex justify-between text-xl">
                <span className="text-muted-foreground">Subtotal</span>
                <span className="font-semibold">${order.totals.subtotal.toFixed(2)}</span>
              </div>
              {order.totals.discounts > 0 && (
                <div className="flex justify-between text-xl text-green-600">
                  <span>Discounts</span>
                  <span>-${order.totals.discounts.toFixed(2)}</span>
                </div>
              )}
              <div className="flex justify-between text-xl">
                <span className="text-muted-foreground">Tax</span>
                <span className="font-semibold">${order.totals.tax.toFixed(2)}</span>
              </div>
              {order.totals.tip && order.totals.tip > 0 && (
                <div className="flex justify-between text-xl">
                  <span className="text-muted-foreground">Tip</span>
                  <span className="font-semibold">${order.totals.tip.toFixed(2)}</span>
                </div>
              )}
              <Separator />
              <div className="flex justify-between items-center">
                <span className="text-2xl font-bold text-foreground">Total</span>
                <span className="text-3xl font-bold text-accent">${order.totals.total.toFixed(2)}</span>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Action Buttons */}
        <div className="flex flex-col items-center gap-4">
          <Button
            size="lg"
            onClick={handleNewOrder}
            className="touch-target text-2xl px-16 py-8 bg-accent hover:bg-accent/90 text-accent-foreground"
          >
            <Home className="w-6 h-6 mr-3" />
            Start New Order
          </Button>
          <div className="flex items-center gap-2 text-xl text-muted-foreground">
            <Clock className="w-5 h-5" />
            <span>Returning to home in {countdown}s</span>
          </div>
        </div>
      </div>
    </div>
  )
}
