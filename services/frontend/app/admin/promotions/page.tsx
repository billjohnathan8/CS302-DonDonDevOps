"use client"

import { useState, useEffect, Fragment } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Badge } from "@/components/ui/badge"
import { PromoForm } from "@/components/admin/promo-form"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useToast } from "@/hooks/use-toast"
import { Tag, RefreshCw, Plus, ChevronDown, ChevronRight, Trash2 } from "lucide-react"
import { format } from "date-fns"
import type { Promotion, ProductAdmin } from "@/lib/types"

type ApplyItemInput = {
  id: string
  productId: string
  quantity: number
  unitPrice: number
}

type ApplyResult = {
  productId: string
  discountRate: number
  discountAmount: number
  finalUnitPrice: number
}

type PromotionProduct = {
  productId: string
  name: string
  stock: number
  unitPrice: number
}

const generateId = () =>
  typeof globalThis !== "undefined" &&
  typeof globalThis.crypto !== "undefined" &&
  typeof globalThis.crypto.randomUUID === "function"
    ? globalThis.crypto.randomUUID()
    : `${Date.now()}-${Math.random()}`

export default function PromotionsPage() {
  const [promotions, setPromotions] = useState<Promotion[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingPromo, setEditingPromo] = useState<Promotion | null>(null)
  const [expandedPromo, setExpandedPromo] = useState<string | null>(null)
  const [products, setProducts] = useState<ProductAdmin[]>([])
  const [applyItems, setApplyItems] = useState<Record<string, ApplyItemInput[]>>({})
  const [applyResults, setApplyResults] = useState<Record<string, ApplyResult[]>>({})
  const [applyLoading, setApplyLoading] = useState<Record<string, boolean>>({})
  const [linkedProducts, setLinkedProducts] = useState<Record<string, PromotionProduct[]>>({})
  const [linkedProductsLoading, setLinkedProductsLoading] = useState<Record<string, boolean>>({})
  const [linkedProductsError, setLinkedProductsError] = useState<Record<string, string | null>>({})
  const { toast } = useToast()

  useEffect(() => {
    fetchPromotions()
    fetchProducts()
  }, [])

  const fetchPromotions = async () => {
    try {
      const response = await fetch("/api/promotions")
      if (!response.ok) {
        throw new Error(await response.text())
      }
      const data = await response.json()
      setPromotions(data)
      setLoading(false)
    } catch (err) {
      console.error("[DonDonDevOps] Failed to fetch promotions:", err)
      toast({ title: "Promotions unavailable", description: "Unable to load promotions.", variant: "destructive" })
      setLoading(false)
    }
  }

  const fetchLinkedProducts = async (promoId: string) => {
    setLinkedProductsError((prev) => ({ ...prev, [promoId]: null }))
    setLinkedProductsLoading((prev) => ({ ...prev, [promoId]: true }))
    try {
      const response = await fetch(`/api/promotions/${promoId}/products`)
      const payload = await response.json()
      if (!response.ok) {
        const message = payload?.message ?? "Failed to load linked products."
        throw new Error(message)
      }
      setLinkedProducts((prev) => ({ ...prev, [promoId]: payload }))
    } catch (error) {
      console.error("[DonDonDevOps] Failed to fetch promotion products:", error)
      const message = error instanceof Error ? error.message : "Unable to load linked products."
      setLinkedProductsError((prev) => ({ ...prev, [promoId]: message }))
    } finally {
      setLinkedProductsLoading((prev) => ({ ...prev, [promoId]: false }))
    }
  }

  const fetchProducts = async () => {
    try {
      const response = await fetch("/api/products")
      if (!response.ok) {
        throw new Error(await response.text())
      }
      const data = await response.json()
      setProducts(data)
    } catch (err) {
      console.error("[DonDonDevOps] Failed to fetch products:", err)
    }
  }

  const handleCreatePromo = async (promo: Omit<Promotion, "promoId">) => {
    try {
      const response = await fetch("/api/promotions", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(promo),
      })
      if (!response.ok) {
        throw new Error(await response.text())
      }
      toast({ title: "Promotion created", description: `${promo.name} is now live.` })
      setShowForm(false)
      fetchPromotions()
    } catch (err) {
      console.error("[DonDonDevOps] Failed to create promotion:", err)
      toast({ title: "Failed to create promotion", description: "Please try again.", variant: "destructive" })
    }
  }

  const handleEditPromo = (promo: Promotion) => {
    setEditingPromo(promo)
    setShowForm(false)
  }

  const handleUpdatePromo = async (updatedFields: Omit<Promotion, "promoId">) => {
    if (!editingPromo) return
    try {
      const response = await fetch(`/api/promotions/${editingPromo.promoId}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(updatedFields),
      })
      if (!response.ok) {
        throw new Error(await response.text())
      }
      toast({ title: "Promotion updated", description: `${updatedFields.name} saved successfully.` })
      setEditingPromo(null)
      fetchPromotions()
    } catch (err) {
      console.error("[DonDonDevOps] Failed to update promotion:", err)
      toast({ title: "Failed to update promotion", description: "Please try again.", variant: "destructive" })
    }
  }

  const handleDeletePromo = async (promoId: string) => {
    if (!confirm("Delete this promotion? This action cannot be undone.")) return
    try {
      const response = await fetch(`/api/promotions/${promoId}`, { method: "DELETE" })
      if (!response.ok && response.status !== 204) {
        throw new Error(await response.text())
      }
      toast({ title: "Promotion deleted" })
      if (editingPromo?.promoId === promoId) {
        setEditingPromo(null)
      }
      fetchPromotions()
    } catch (err) {
      console.error("[DonDonDevOps] Failed to delete promotion:", err)
      toast({ title: "Failed to delete promotion", description: "Please try again.", variant: "destructive" })
    }
  }

  const toggleExpanded = (promoId: string) => {
    const next = expandedPromo === promoId ? null : promoId
    setExpandedPromo(next)
    if (next && !linkedProducts[next] && !linkedProductsLoading[next]) {
      fetchLinkedProducts(next)
    }
  }

  const handleAddApplyItem = (promoId: string) => {
    if (products.length === 0) {
      toast({
        title: "No products",
        description: "Add inventory products before testing promotions.",
        variant: "destructive",
      })
      return
    }
    const defaultProduct = products[0]
    setApplyItems((prev) => ({
      ...prev,
      [promoId]: [
        ...(prev[promoId] ?? []),
        {
          id: generateId(),
          productId: defaultProduct.ProductID,
          quantity: 1,
          unitPrice: Number(defaultProduct.priceSGD.toFixed(2)),
        },
      ],
    }))
  }

  const handleUpdateApplyItem = (promoId: string, itemId: string, updates: Partial<ApplyItemInput>) => {
    setApplyItems((prev) => ({
      ...prev,
      [promoId]: (prev[promoId] ?? []).map((item) => (item.id === itemId ? { ...item, ...updates } : item)),
    }))
  }

  const handleRemoveApplyItem = (promoId: string, itemId: string) => {
    setApplyItems((prev) => ({
      ...prev,
      [promoId]: (prev[promoId] ?? []).filter((item) => item.id !== itemId),
    }))
    setApplyResults((prev) => ({
      ...prev,
      [promoId]: [],
    }))
  }

  const handleProductChange = (promoId: string, itemId: string, productId: string) => {
    const selectedProduct = products.find((p) => p.ProductID === productId)
    handleUpdateApplyItem(promoId, itemId, {
      productId,
      unitPrice: selectedProduct ? Number(selectedProduct.priceSGD.toFixed(2)) : 0,
    })
  }

  const handleApplyPromotions = async (promoId: string) => {
    const items = applyItems[promoId] ?? []
    if (items.length === 0) {
      toast({ title: "Add items", description: "Add at least one item before applying.", variant: "destructive" })
      return
    }

    setApplyLoading((prev) => ({ ...prev, [promoId]: true }))
    try {
      const promotion = promotions.find((promo) => promo.promoId === promoId)
      const ensurePromotionIsLive = async () => {
        if (!promotion) return
        const start = new Date(promotion.startDate)
        if (Number.isNaN(start.getTime()) || start <= new Date()) {
          return
        }
        try {
          const response = await fetch(`/api/promotions/${promoId}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ startDate: new Date().toISOString() }),
          })
          if (!response.ok) {
            throw new Error(await response.text())
          }
          const updated: Promotion = await response.json()
          setPromotions((prev) => prev.map((promo) => (promo.promoId === updated.promoId ? updated : promo)))
          toast({
            title: "Promotion activated",
            description: `${updated.name} is now live on the kiosk.`,
          })
        } catch (error) {
          console.error("[DonDonDevOps] Failed to auto-activate promotion:", error)
          toast({
            title: "Failed to activate promotion",
            description: "Update its start time or edit the promotion to make it live.",
            variant: "destructive",
          })
        }
      }

      const uniqueProductIds = Array.from(new Set(items.map((item) => item.productId)))

      const linkResponse = await fetch(`/api/promotions/${promoId}/products`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ productIds: uniqueProductIds }),
      })
      const linkPayload = await linkResponse.json()
      if (!linkResponse.ok) {
        throw new Error(linkPayload?.message ?? "Failed to link products to promotion.")
      }

      await fetchLinkedProducts(promoId)
      await ensurePromotionIsLive()

      const applyPayloadItems = items.map((item) => {
        const product = products.find((p) => p.ProductID === item.productId)
        const unitPrice = product ? Number(product.priceSGD) : item.unitPrice
        return {
          productId: item.productId,
          quantity: item.quantity,
          unitPrice,
        }
      })

      const response = await fetch("/api/promotions/apply", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          now: new Date().toISOString(),
          items: applyPayloadItems,
        }),
      })

      if (!response.ok) {
        throw new Error(await response.text())
      }

      const data = await response.json()
      setApplyResults((prev) => ({
        ...prev,
        [promoId]: data.items,
      }))
      toast({ title: "Products linked", description: "Discount breakdown updated below." })
    } catch (err) {
      console.error("[DonDonDevOps] Failed to apply promotions:", err)
      toast({ title: "Failed to apply", description: "Try again later.", variant: "destructive" })
    } finally {
      setApplyLoading((prev) => ({ ...prev, [promoId]: false }))
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold">Promotions</h2>
          <p className="text-lg text-muted-foreground mt-1">Manage discounts and special offers</p>
        </div>
        <div className="flex gap-3">
          <Button size="lg" onClick={fetchPromotions} variant="outline" className="text-lg px-6 py-6 bg-transparent">
            <RefreshCw className="w-5 h-5 mr-2" />
            Refresh
          </Button>
          <Button
            size="lg"
            onClick={() => setShowForm(!showForm)}
            className="text-lg px-6 py-6 bg-accent hover:bg-accent/90"
          >
            <Plus className="w-5 h-5 mr-2" />
            New Promotion
          </Button>
        </div>
      </div>

      {showForm && <PromoForm onSubmit={handleCreatePromo} onCancel={() => setShowForm(false)} />}
      {editingPromo && (
        <PromoForm
          onSubmit={handleUpdatePromo}
          onCancel={() => setEditingPromo(null)}
          initialPromotion={editingPromo}
          mode="edit"
        />
      )}

      <Card className="border-2">
        <CardHeader>
          <CardTitle className="text-2xl">Active Promotions</CardTitle>
        </CardHeader>
        <CardContent>
          <ScrollArea className="h-[calc(100vh-24rem)]">
            {loading ? (
              <div className="text-center py-12">
                <Tag className="w-12 h-12 animate-pulse mx-auto mb-4 text-accent" />
                <p className="text-xl text-muted-foreground">Loading promotions...</p>
              </div>
            ) : promotions.length === 0 ? (
              <div className="text-center py-12">
                <Tag className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
                <p className="text-xl text-muted-foreground">No active promotions</p>
              </div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-12"></TableHead>
                    <TableHead className="text-lg">Name</TableHead>
                    <TableHead className="text-lg">Discount</TableHead>
                    <TableHead className="text-lg">Start Time</TableHead>
                    <TableHead className="text-lg">End Time</TableHead>
                    <TableHead className="text-lg">Status</TableHead>
                    <TableHead className="text-lg text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {promotions.map((promo) => {
                    const now = new Date()
                    const start = new Date(promo.startDate)
                    const end = new Date(promo.endDate)
                    const isActive = now >= start && now <= end
                    const isUpcoming = now < start
                    const isExpired = now > end
                    const isExpanded = expandedPromo === promo.promoId
                    const items = applyItems[promo.promoId] ?? []
                    const results = applyResults[promo.promoId] ?? []
                    const isApplying = applyLoading[promo.promoId]
                    const promoLinkedProducts = linkedProducts[promo.promoId] ?? []
                    const promoLinkedProductsLoading = linkedProductsLoading[promo.promoId]
                    const promoLinkedProductsError = linkedProductsError[promo.promoId]

                    return (
                      <Fragment key={promo.promoId}>
                        <TableRow key={promo.promoId} className="hover:bg-muted/50">
                          <TableCell>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => toggleExpanded(promo.promoId)}
                              className="p-1 h-8 w-8"
                            >
                              {isExpanded ? <ChevronDown className="w-5 h-5" /> : <ChevronRight className="w-5 h-5" />}
                            </Button>
                          </TableCell>
                          <TableCell className="text-lg font-medium">{promo.name}</TableCell>
                          <TableCell>
                            <Badge className="bg-green-500 text-white text-base px-3 py-1">
                              {Math.round(promo.discountRate * 100)}% OFF
                            </Badge>
                          </TableCell>
                          <TableCell className="text-base">{format(start, "MMM dd, yyyy HH:mm")}</TableCell>
                          <TableCell className="text-base">{format(end, "MMM dd, yyyy HH:mm")}</TableCell>
                          <TableCell>
                            {isActive && (
                              <Badge className="bg-green-100 text-green-600 text-base px-3 py-1">Active</Badge>
                            )}
                            {isUpcoming && (
                              <Badge className="bg-blue-100 text-blue-600 text-base px-3 py-1">Upcoming</Badge>
                            )}
                            {isExpired && (
                              <Badge className="bg-gray-100 text-gray-600 text-base px-3 py-1">Expired</Badge>
                            )}
                          </TableCell>
                          <TableCell className="text-right space-x-2">
                            <Button variant="outline" size="sm" onClick={() => handleEditPromo(promo)}>
                              Edit
                            </Button>
                            <Button variant="destructive" size="sm" onClick={() => handleDeletePromo(promo.promoId)}>
                              Delete
                            </Button>
                          </TableCell>
                        </TableRow>
                        {isExpanded && (
                          <TableRow key={`${promo.promoId}-details`}>
                            <TableCell colSpan={7} className="bg-muted/30">
                              <div className="p-4 space-y-6">
                                <div className="space-y-3">
                                  <div className="flex items-center justify-between">
                                    <h4 className="font-semibold text-lg">Linked Products</h4>
                                    <Button
                                      size="sm"
                                      variant="outline"
                                      onClick={() => fetchLinkedProducts(promo.promoId)}
                                      disabled={Boolean(promoLinkedProductsLoading)}
                                    >
                                      <RefreshCw className="w-4 h-4 mr-2" />
                                      Refresh
                                    </Button>
                                  </div>
                                  {promoLinkedProductsLoading ? (
                                    <p className="text-sm text-muted-foreground">Loading linked products...</p>
                                  ) : promoLinkedProductsError ? (
                                    <p className="text-sm text-destructive">{promoLinkedProductsError}</p>
                                  ) : promoLinkedProducts.length === 0 ? (
                                    <p className="text-sm text-muted-foreground">
                                      No products linked to this promotion yet.
                                    </p>
                                  ) : (
                                    <Table>
                                      <TableHeader>
                                        <TableRow>
                                          <TableHead>Product</TableHead>
                                          <TableHead>Stock</TableHead>
                                          <TableHead>Unit Price (SGD)</TableHead>
                                          <TableHead>Price After Discount</TableHead>
                                        </TableRow>
                                      </TableHeader>
                                      <TableBody>
                                        {promoLinkedProducts.map((product) => {
                                          const discountedPrice = Math.max(0, product.unitPrice * (1 - promo.discountRate))
                                          return (
                                            <TableRow key={product.productId}>
                                              <TableCell className="font-medium">{product.name}</TableCell>
                                              <TableCell>{product.stock}</TableCell>
                                              <TableCell>${product.unitPrice.toFixed(2)}</TableCell>
                                              <TableCell>${discountedPrice.toFixed(2)}</TableCell>
                                            </TableRow>
                                          )
                                        })}
                                      </TableBody>
                                    </Table>
                                  )}
                                </div>

                                <p className="text-sm text-muted-foreground">
                                  Build a basket with inventory products and run the /promotions/apply endpoint.
                                </p>
                                <div className="space-y-3">
                                  <Table>
                                    <TableHeader>
                                      <TableRow>
                                        <TableHead className="text-sm">Product</TableHead>
                                        <TableHead className="text-sm">Actions</TableHead>
                                      </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                      {items.length === 0 ? (
                                        <TableRow>
                                          <TableCell colSpan={2} className="text-center text-sm text-muted-foreground">
                                            No items yet. Add products to test this promotion.
                                          </TableCell>
                                        </TableRow>
                                      ) : (
                                        items.map((item) => (
                                          <TableRow key={item.id}>
                                            <TableCell>
                                              <Select
                                                value={item.productId}
                                                onValueChange={(value) => handleProductChange(promo.promoId, item.id, value)}
                                              >
                                                <SelectTrigger className="w-60">
                                                  <SelectValue placeholder="Select product" />
                                                </SelectTrigger>
                                                <SelectContent>
                                                  {products.map((product) => (
                                                    <SelectItem key={product.ProductID} value={product.ProductID}>
                                                      {product.name}
                                                    </SelectItem>
                                                  ))}
                                                </SelectContent>
                                              </Select>
                                            </TableCell>
                                            <TableCell>
                                              <Button
                                                size="sm"
                                                variant="ghost"
                                                onClick={() => handleRemoveApplyItem(promo.promoId, item.id)}
                                              >
                                                <Trash2 className="w-4 h-4" />
                                              </Button>
                                            </TableCell>
                                          </TableRow>
                                        ))
                                      )}
                                    </TableBody>
                                  </Table>
                                  <div className="flex gap-3">
                                    <Button variant="outline" onClick={() => handleAddApplyItem(promo.promoId)}>
                                      Add Item
                                    </Button>
                                    <Button
                                      onClick={() => handleApplyPromotions(promo.promoId)}
                                      disabled={isApplying || items.length === 0}
                                      className="bg-accent hover:bg-accent/90"
                                    >
                                      {isApplying ? "Applying..." : "Apply Promotions"}
                                    </Button>
                                  </div>
                                </div>
                                {results.length > 0 && (
                                  <div className="space-y-2">
                                    <h5 className="font-semibold">Discount results</h5>
                                    <Table>
                                      <TableHeader>
                                        <TableRow>
                                          <TableHead>Product</TableHead>
                                          <TableHead>Brand</TableHead>
                                          <TableHead>Stock</TableHead>
                                          <TableHead>Unit Price (SGD)</TableHead>
                                          <TableHead>Discount %</TableHead>
                                          <TableHead>Discount Value</TableHead>
                                          <TableHead>Price After Discount</TableHead>
                                        </TableRow>
                                      </TableHeader>
                                      <TableBody>
                                        {results.map((result) => {
                                          const product = products.find((p) => p.ProductID === result.productId)
                                          const unitPrice = product
                                            ? product.priceSGD
                                            : result.finalUnitPrice + result.discountAmount
                                          const discountPercent = Math.round(result.discountRate * 100)
                                          return (
                                            <TableRow key={`${result.productId}-${result.finalUnitPrice}`}>
                                              <TableCell>{product?.name ?? result.productId}</TableCell>
                                              <TableCell>{product?.brand ?? "—"}</TableCell>
                                              <TableCell>{product?.stock ?? "—"}</TableCell>
                                              <TableCell>${unitPrice.toFixed(2)}</TableCell>
                                              <TableCell>{discountPercent}%</TableCell>
                                              <TableCell>${result.discountAmount.toFixed(2)}</TableCell>
                                              <TableCell>${result.finalUnitPrice.toFixed(2)}</TableCell>
                                            </TableRow>
                                          )
                                        })}
                                      </TableBody>
                                    </Table>
                                  </div>
                                )}
                              </div>
                            </TableCell>
                          </TableRow>
                        )}
                      </Fragment>
                    )
                  })}
                </TableBody>
              </Table>
            )}
          </ScrollArea>
        </CardContent>
      </Card>
    </div>
  )
}
