"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Badge } from "@/components/ui/badge"
import { ShoppingBag, RefreshCw } from "lucide-react"

type PaymentRecord = {
  id: string
  totalPrice: number
  paid: boolean
}

export default function OrdersPage() {
  const [orders, setOrders] = useState<PaymentRecord[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchOrders()
  }, [])

  const fetchOrders = async () => {
    try {
      const response = await fetch("/api/payments/history")
      if (!response.ok) {
        throw new Error(await response.text())
      }
      const data = (await response.json()) as PaymentRecord[]
      setOrders(data)
      setLoading(false)
    } catch (err) {
      console.error("[DonDonDevOps] Failed to fetch orders:", err)
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold">Order History</h2>
          <p className="text-lg text-muted-foreground mt-1">View all orders and receipts</p>
        </div>
        <Button size="lg" onClick={fetchOrders} variant="outline" className="text-lg px-6 py-6 bg-transparent">
          <RefreshCw className="w-5 h-5 mr-2" />
          Refresh
        </Button>
      </div>

      <Card className="border-2">
        <CardHeader>
          <CardTitle className="text-2xl">All Orders</CardTitle>
        </CardHeader>
        <CardContent>
          <ScrollArea className="h-[calc(100vh-20rem)]">
            {loading ? (
              <div className="text-center py-12">
                <ShoppingBag className="w-12 h-12 animate-pulse mx-auto mb-4 text-accent" />
                <p className="text-xl text-muted-foreground">Loading orders...</p>
              </div>
            ) : orders.length === 0 ? (
              <div className="text-center py-12">
                <ShoppingBag className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
                <p className="text-xl text-muted-foreground">No orders yet</p>
              </div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="text-lg">Order ID</TableHead>
                    <TableHead className="text-lg">Total (SGD)</TableHead>
                    <TableHead className="text-lg">Status</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {orders.map((order) => (
                    <TableRow key={order.id} className="hover:bg-muted/50">
                      <TableCell className="text-lg font-medium truncate">{order.id}</TableCell>
                      <TableCell className="text-lg font-semibold">${order.totalPrice.toFixed(2)}</TableCell>
                      <TableCell>
                        <Badge className={order.paid ? "bg-green-100 text-green-700" : "bg-yellow-100 text-yellow-700"}>
                          {order.paid ? "Paid" : "Pending"}
                        </Badge>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </ScrollArea>
        </CardContent>
      </Card>

    </div>
  )
}
