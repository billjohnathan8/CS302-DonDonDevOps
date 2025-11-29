"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { ScrollArea } from "@/components/ui/scroll-area"
import { ExpiryBadge } from "@/components/admin/expiry-badge"
import { DiscountBadge } from "@/components/admin/discount-badge"
import { DiscountDialog } from "@/components/admin/discount-dialog"
import { Clock, RefreshCw } from "lucide-react"
import type { ExpiringItem } from "@/lib/types"

export default function ExpiringPage() {
  const [expiringItems, setExpiringItems] = useState<ExpiringItem[]>([])
  const [loading, setLoading] = useState(true)
  const [discountDialogOpen, setDiscountDialogOpen] = useState(false)
  const [selectedItem, setSelectedItem] = useState<ExpiringItem | null>(null)

  useEffect(() => {
    fetchExpiringItems()
  }, [])

  const fetchExpiringItems = async () => {
    try {
      const response = await fetch("/api/inventory/expiring")
      const data = await response.json()
      setExpiringItems(data)
      setLoading(false)
    } catch (err) {
      console.error("[DonDonDevOps] Failed to fetch expiring items:", err)
      setLoading(false)
    }
  }

  const handleSetDiscount = (item: ExpiringItem) => {
    setSelectedItem(item)
    setDiscountDialogOpen(true)
  }

  const handleDiscountApply = async (discountRate: number) => {
    if (!selectedItem) return

    try {
      await fetch(`/api/products/${selectedItem.product.id}/discount`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ discountRate }),
      })
      alert("Discount applied successfully!")
      fetchExpiringItems()
    } catch (err) {
      console.error("[DonDonDevOps] Failed to apply discount:", err)
      alert("Failed to apply discount")
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold">Expiring Products</h2>
          <p className="text-lg text-muted-foreground mt-1">Items nearing expiration date</p>
        </div>
        <Button size="lg" onClick={fetchExpiringItems} variant="outline" className="text-lg px-6 py-6 bg-transparent">
          <RefreshCw className="w-5 h-5 mr-2" />
          Refresh
        </Button>
      </div>

      <Card className="border-2">
        <CardHeader>
          <CardTitle className="text-2xl">Near-Expiry Items</CardTitle>
        </CardHeader>
        <CardContent>
          <ScrollArea className="h-[calc(100vh-20rem)]">
            {loading ? (
              <div className="text-center py-12">
                <Clock className="w-12 h-12 animate-pulse mx-auto mb-4 text-accent" />
                <p className="text-xl text-muted-foreground">Loading expiring items...</p>
              </div>
            ) : expiringItems.length === 0 ? (
              <div className="text-center py-12">
                <Clock className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
                <p className="text-xl text-muted-foreground">No items expiring soon!</p>
              </div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="text-lg">Product</TableHead>
                    <TableHead className="text-lg">Expires On</TableHead>
                    <TableHead className="text-lg">Quantity</TableHead>
                    <TableHead className="text-lg">Current Price</TableHead>
                    <TableHead className="text-lg">Status</TableHead>
                    <TableHead className="text-lg">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {expiringItems.map((item) => {
                    const hasDiscount = item.product.promoPrice && item.product.promoPrice < item.product.basePrice
                    const discountRate = hasDiscount
                      ? (item.product.basePrice - item.product.promoPrice!) / item.product.basePrice
                      : 0

                    return (
                      <TableRow key={item.product.id} className="hover:bg-muted/50">
                        <TableCell className="text-lg font-medium">{item.product.name}</TableCell>
                        <TableCell>
                          <ExpiryBadge daysUntilExpiry={item.daysUntilExpiry} />
                        </TableCell>
                        <TableCell className="text-lg">{item.qty}</TableCell>
                        <TableCell className="text-lg font-semibold">${item.currentPrice.toFixed(2)}</TableCell>
                        <TableCell>
                          {hasDiscount ? (
                            <DiscountBadge discountRate={discountRate} />
                          ) : (
                            <span className="text-muted-foreground">No discount</span>
                          )}
                        </TableCell>
                        <TableCell>
                          <Button
                            size="lg"
                            onClick={() => handleSetDiscount(item)}
                            className="text-base px-6 py-4 bg-accent hover:bg-accent/90"
                          >
                            Set Discount
                          </Button>
                        </TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            )}
          </ScrollArea>
        </CardContent>
      </Card>

      {selectedItem && (
        <DiscountDialog
          open={discountDialogOpen}
          onOpenChange={setDiscountDialogOpen}
          productName={selectedItem.product.name}
          currentPrice={selectedItem.currentPrice}
          onApply={handleDiscountApply}
        />
      )}
    </div>
  )
}
