"use client"

import { useEffect, useState } from "react"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ScrollArea } from "@/components/ui/scroll-area"
import { CheckCircle2, Clock, Loader2, Package } from "lucide-react"
import type { Order } from "@/lib/types"

export default function StatusBoardPage() {
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Initial fetch
    fetchOrders()

    // Poll for updates every 3 seconds
    const interval = setInterval(fetchOrders, 3000)

    return () => clearInterval(interval)
  }, [])

  const fetchOrders = async () => {
    try {
      const response = await fetch("/api/orders")
      const data = await response.json()
      setOrders(data.filter((order: Order) => order.status !== "completed"))
      setLoading(false)
    } catch (err) {
      console.error("[DonDonDevOps] Failed to fetch orders:", err)
      setLoading(false)
    }
  }

  const statusConfig = {
    placed: {
      icon: Clock,
      label: "Placed",
      color: "text-blue-600",
      bgColor: "bg-blue-100",
      borderColor: "border-blue-300",
    },
    preparing: {
      icon: Loader2,
      label: "Preparing",
      color: "text-accent",
      bgColor: "bg-accent/10",
      borderColor: "border-accent",
    },
    ready: {
      icon: CheckCircle2,
      label: "Ready",
      color: "text-green-600",
      bgColor: "bg-green-100",
      borderColor: "border-green-300",
    },
    completed: {
      icon: CheckCircle2,
      label: "Completed",
      color: "text-green-600",
      bgColor: "bg-green-100",
      borderColor: "border-green-300",
    },
  }

  const groupedOrders = {
    placed: orders.filter((o) => o.status === "placed"),
    preparing: orders.filter((o) => o.status === "preparing"),
    ready: orders.filter((o) => o.status === "ready"),
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-center space-y-4">
          <div className="w-16 h-16 border-4 border-accent border-t-transparent rounded-full animate-spin mx-auto" />
          <p className="text-2xl text-muted-foreground">Loading orders...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-secondary p-8">
      {/* Header */}
      <div className="mb-8 text-center">
        <h1 className="text-6xl font-bold text-foreground mb-3">Order Status Board</h1>
        <p className="text-2xl text-muted-foreground">Track your order in real-time</p>
      </div>

      {/* Status Columns */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-7xl mx-auto">
        {/* Placed Orders */}
        <div className="space-y-4">
          <div className="flex items-center gap-3 mb-4">
            <Clock className="w-8 h-8 text-blue-600" />
            <h2 className="text-3xl font-bold text-foreground">Placed</h2>
            <Badge className="bg-blue-100 text-blue-600 text-xl px-4 py-1">{groupedOrders.placed.length}</Badge>
          </div>
          <ScrollArea className="h-[calc(100vh-250px)]">
            <div className="space-y-4 pr-4">
              {groupedOrders.placed.length === 0 ? (
                <Card className="border-2 border-dashed border-muted">
                  <CardContent className="p-8 text-center">
                    <Package className="w-12 h-12 text-muted-foreground mx-auto mb-3" />
                    <p className="text-xl text-muted-foreground">No orders</p>
                  </CardContent>
                </Card>
              ) : (
                groupedOrders.placed.map((order) => (
                  <Card key={order.id} className="border-4 border-blue-300 shadow-lg">
                    <CardContent className="p-6 space-y-3">
                      <div className="flex items-center justify-between">
                        <p className="text-5xl font-bold text-accent">{order.number}</p>
                        <Badge className="bg-blue-100 text-blue-600 text-lg px-4 py-2">
                          {statusConfig.placed.label}
                        </Badge>
                      </div>
                      <div className="text-lg text-muted-foreground">
                        {order.items.length} item{order.items.length !== 1 ? "s" : ""}
                      </div>
                      <div className="text-sm text-muted-foreground">
                        {new Date(order.createdAt).toLocaleTimeString()}
                      </div>
                    </CardContent>
                  </Card>
                ))
              )}
            </div>
          </ScrollArea>
        </div>

        {/* Preparing Orders */}
        <div className="space-y-4">
          <div className="flex items-center gap-3 mb-4">
            <Loader2 className="w-8 h-8 text-accent animate-spin" />
            <h2 className="text-3xl font-bold text-foreground">Preparing</h2>
            <Badge className="bg-accent/10 text-accent text-xl px-4 py-1">{groupedOrders.preparing.length}</Badge>
          </div>
          <ScrollArea className="h-[calc(100vh-250px)]">
            <div className="space-y-4 pr-4">
              {groupedOrders.preparing.length === 0 ? (
                <Card className="border-2 border-dashed border-muted">
                  <CardContent className="p-8 text-center">
                    <Package className="w-12 h-12 text-muted-foreground mx-auto mb-3" />
                    <p className="text-xl text-muted-foreground">No orders</p>
                  </CardContent>
                </Card>
              ) : (
                groupedOrders.preparing.map((order) => (
                  <Card key={order.id} className="border-4 border-accent shadow-lg">
                    <CardContent className="p-6 space-y-3">
                      <div className="flex items-center justify-between">
                        <p className="text-5xl font-bold text-accent">{order.number}</p>
                        <Badge className="bg-accent/10 text-accent text-lg px-4 py-2">
                          {statusConfig.preparing.label}
                        </Badge>
                      </div>
                      <div className="text-lg text-muted-foreground">
                        {order.items.length} item{order.items.length !== 1 ? "s" : ""}
                      </div>
                      <div className="text-sm text-muted-foreground">
                        {new Date(order.createdAt).toLocaleTimeString()}
                      </div>
                    </CardContent>
                  </Card>
                ))
              )}
            </div>
          </ScrollArea>
        </div>

        {/* Ready Orders */}
        <div className="space-y-4">
          <div className="flex items-center gap-3 mb-4">
            <CheckCircle2 className="w-8 h-8 text-green-600" />
            <h2 className="text-3xl font-bold text-foreground">Ready</h2>
            <Badge className="bg-green-100 text-green-600 text-xl px-4 py-1">{groupedOrders.ready.length}</Badge>
          </div>
          <ScrollArea className="h-[calc(100vh-250px)]">
            <div className="space-y-4 pr-4">
              {groupedOrders.ready.length === 0 ? (
                <Card className="border-2 border-dashed border-muted">
                  <CardContent className="p-8 text-center">
                    <Package className="w-12 h-12 text-muted-foreground mx-auto mb-3" />
                    <p className="text-xl text-muted-foreground">No orders</p>
                  </CardContent>
                </Card>
              ) : (
                groupedOrders.ready.map((order) => (
                  <Card key={order.id} className="border-4 border-green-300 shadow-lg animate-pulse">
                    <CardContent className="p-6 space-y-3">
                      <div className="flex items-center justify-between">
                        <p className="text-5xl font-bold text-accent">{order.number}</p>
                        <Badge className="bg-green-100 text-green-600 text-lg px-4 py-2">
                          {statusConfig.ready.label}
                        </Badge>
                      </div>
                      <div className="text-lg text-muted-foreground">
                        {order.items.length} item{order.items.length !== 1 ? "s" : ""}
                      </div>
                      <div className="text-sm text-muted-foreground">
                        {new Date(order.createdAt).toLocaleTimeString()}
                      </div>
                    </CardContent>
                  </Card>
                ))
              )}
            </div>
          </ScrollArea>
        </div>
      </div>
    </div>
  )
}
