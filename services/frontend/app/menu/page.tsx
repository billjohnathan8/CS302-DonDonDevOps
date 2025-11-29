"use client"

import type React from "react"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Separator } from "@/components/ui/separator"
import { Input } from "@/components/ui/input"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { ShoppingCart, Sparkles, TrendingUp, Search, X, Plus, Info, ArrowLeft } from "lucide-react"
import { useCartStore } from "@/lib/store"
import type { Product, MenuResponse } from "@/lib/types"

export default function MenuPage() {
  const router = useRouter()
  const [menuData, setMenuData] = useState<MenuResponse | null>(null)
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null)
  const [searchQuery, setSearchQuery] = useState("")
  const [loading, setLoading] = useState(true)
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null)
  const [productQuantities, setProductQuantities] = useState<Record<string, number>>({})
  const cartLines = useCartStore((state) => state.lines)
  const addLine = useCartStore((state) => state.addLine)

  useEffect(() => {
    fetch("/api/menu")
      .then((res) => res.json())
      .then((data: MenuResponse) => {
        setMenuData(data)
        if (data.categories.length > 0) {
          setSelectedCategory(data.categories[0].id)
        }
        setLoading(false)
      })
      .catch((err) => {
        console.error("[DonDonDevOps] Failed to load menu:", err)
        setLoading(false)
      })
  }, [])

  const handleViewCart = () => {
    router.push("/cart")
  }

  const handleBackToHome = () => {
    router.push("/")
  }

  const handleClearSearch = () => {
    setSearchQuery("")
  }

  const handleQuickAdd = (product: Product, e: React.MouseEvent) => {
    e.stopPropagation()
    const effectivePrice = product.promoPrice ?? product.basePrice
    addLine({
      productId: product.id,
      name: product.name,
      qty: 1,
      unitPrice: effectivePrice,
      baseUnitPrice: product.basePrice,
      promoName: product.promoName,
    })
    // Show feedback by incrementing local quantity
    setProductQuantities((prev) => ({ ...prev, [product.id]: (prev[product.id] || 0) + 1 }))
    setTimeout(() => {
      setProductQuantities((prev) => {
        const { [product.id]: _, ...rest } = prev
        return rest
      })
    }, 1000)
  }

  const getProductQtyInCart = (productId: string): number => {
    return cartLines.find((line) => line.productId === productId)?.qty || 0
  }

  const handleViewDetails = (product: Product, e: React.MouseEvent) => {
    e.stopPropagation()
    setSelectedProduct(product)
  }

  if (loading) {
    return (
      <div className="kiosk-mode min-h-screen bg-background flex items-center justify-center">
        <div className="text-center space-y-4">
          <div className="w-16 h-16 border-4 border-accent border-t-transparent rounded-full animate-spin mx-auto" />
          <p className="text-2xl text-muted-foreground">Loading menu...</p>
        </div>
      </div>
    )
  }

  if (!menuData) {
    return (
      <div className="kiosk-mode min-h-screen bg-background flex items-center justify-center">
        <p className="text-2xl text-destructive">Failed to load menu</p>
      </div>
    )
  }

  let filteredProducts = menuData.products

  if (searchQuery.trim()) {
    const query = searchQuery.toLowerCase()
    filteredProducts = filteredProducts.filter(
      (p) => p.name.toLowerCase().includes(query) || (p.brand && p.brand.toLowerCase().includes(query)),
    )
  } else if (selectedCategory) {
    filteredProducts = filteredProducts.filter((p) => p.categoryId === selectedCategory)
  }

  const cartItemCount = cartLines.reduce((sum, line) => sum + line.qty, 0)

  const getDiscountInfo = (product: Product) => {
    const isDiscounted = product.promoPrice !== undefined && product.promoPrice < product.basePrice
    const effectivePrice = isDiscounted ? product.promoPrice! : product.basePrice
    const badgePercent =
      product.discountPercent ?? (isDiscounted ? Math.round((1 - product.promoPrice! / product.basePrice) * 100) : 0)
    return { isDiscounted, effectivePrice, badgePercent }
  }

  return (
    <div className="kiosk-mode min-h-screen bg-background flex flex-col">
      <header className="bg-card border-b-4 border-border shadow-lg">
        <div className="container mx-auto px-8 py-6 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Button
                size="lg"
                variant="outline"
                onClick={handleBackToHome}
                className="touch-target text-xl px-6 py-6 border-2 hover:bg-secondary bg-transparent"
              >
                <ArrowLeft className="w-6 h-6 mr-2" />
                Back
              </Button>
              <div>
                <h1 className="text-4xl font-bold text-foreground">Our Menu</h1>
                <p className="text-xl text-muted-foreground mt-1">Choose your favorites</p>
              </div>
            </div>
            <Button
              size="lg"
              onClick={handleViewCart}
              className="touch-target relative text-xl px-8 py-6 bg-accent hover:bg-accent/90 text-accent-foreground"
              disabled={cartItemCount === 0}
            >
              <ShoppingCart className="w-6 h-6 mr-3" />
              View Cart
              {cartItemCount > 0 && (
                <Badge className="absolute -top-2 -right-2 bg-destructive text-destructive-foreground text-lg px-3 py-1 rounded-full">
                  {cartItemCount}
                </Badge>
              )}
            </Button>
          </div>

          <div className="relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-6 h-6 text-muted-foreground" />
            <Input
              type="text"
              placeholder="Search menu items..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="touch-target w-full pl-14 pr-14 py-6 text-xl border-2 focus:border-accent"
            />
            {searchQuery && (
              <Button
                variant="ghost"
                size="sm"
                onClick={handleClearSearch}
                className="absolute right-2 top-1/2 -translate-y-1/2 touch-target"
              >
                <X className="w-6 h-6" />
              </Button>
            )}
          </div>
        </div>
      </header>

      <div className="flex-1 flex">
        <aside className="w-80 bg-card border-r-4 border-border">
          <ScrollArea className="h-full">
            <div className="p-6 space-y-3">
              <h2 className="text-2xl font-semibold text-foreground mb-4">Categories</h2>
              {searchQuery ? (
                <div className="p-4 bg-accent/10 border-2 border-accent rounded-lg">
                  <p className="text-xl font-semibold text-accent">Search Results</p>
                  <p className="text-sm text-muted-foreground mt-1">{filteredProducts.length} items found</p>
                </div>
              ) : (
                menuData.categories.map((category) => (
                  <Button
                    key={category.id}
                    onClick={() => setSelectedCategory(category.id)}
                    variant={selectedCategory === category.id ? "default" : "outline"}
                    size="lg"
                    className={`touch-target w-full justify-start text-xl py-6 ${
                      selectedCategory === category.id
                        ? "bg-accent text-accent-foreground hover:bg-accent/90"
                        : "hover:bg-secondary"
                    }`}
                  >
                    {category.name}
                  </Button>
                ))
              )}
            </div>
          </ScrollArea>
        </aside>

        <main className="flex-1 p-8">
          <ScrollArea className="h-full">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 pb-8">
              {filteredProducts.map((product) => {
                const { isDiscounted, effectivePrice, badgePercent } = getDiscountInfo(product)
                const qtyInCart = getProductQtyInCart(product.id)
                const showFeedback = productQuantities[product.id] > 0

                return (
                  <Card
                    key={product.id}
                    className="touch-target hover:shadow-xl transition-all duration-300 border-2 hover:border-accent overflow-hidden"
                  >
                    <CardContent className="p-6">
                      <div className="space-y-3">
                        <div className="flex items-start justify-between gap-3">
                          <div className="flex-1 space-y-2">
                            <h3 className="text-2xl font-semibold text-foreground text-balance">{product.name}</h3>
                            {product.brand && (
                              <p className="text-lg text-muted-foreground font-medium">{product.brand}</p>
                            )}
                          </div>
                          <div className="text-right whitespace-nowrap flex-shrink-0">
                            {isDiscounted ? (
                              <>
                                <p className="text-lg text-muted-foreground line-through">
                                  ${product.basePrice.toFixed(2)}
                                </p>
                                <p className="text-2xl font-bold text-destructive">${effectivePrice.toFixed(2)}</p>
                              </>
                            ) : (
                              <p className="text-2xl font-bold text-accent">${product.basePrice.toFixed(2)}</p>
                            )}
                          </div>
                        </div>

                        <div className="flex flex-wrap gap-2">
                          {isDiscounted && (
                            <Badge className="bg-destructive text-destructive-foreground text-base px-3 py-1 font-bold">
                              -{badgePercent}%
                            </Badge>
                          )}
                          {product.flags?.includes("popular") && (
                            <Badge className="bg-accent text-accent-foreground text-sm px-3 py-1">
                              <TrendingUp className="w-4 h-4 mr-1" />
                              Popular
                            </Badge>
                          )}
                          {product.flags?.includes("new") && (
                            <Badge className="bg-primary text-primary-foreground text-sm px-3 py-1">
                              <Sparkles className="w-4 h-4 mr-1" />
                              New
                            </Badge>
                          )}
                          {product.stock &&
                            product.stock.lowStockThreshold &&
                            product.stock.qty <= product.stock.lowStockThreshold && (
                              <Badge className="bg-destructive text-destructive-foreground text-sm px-3 py-1">
                                Low Stock
                              </Badge>
                            )}
                        </div>

                        <Separator />

                        <div className="flex gap-2 pt-2">
                          <Button
                            size="lg"
                            onClick={(e) => handleQuickAdd(product, e)}
                            className="touch-target flex-1 text-lg py-6 bg-accent hover:bg-accent/90 text-accent-foreground"
                          >
                            <Plus className="w-5 h-5 mr-2" />
                            {showFeedback ? "Added!" : "Add"}
                          </Button>
                          <Button
                            size="lg"
                            variant="outline"
                            onClick={(e) => handleViewDetails(product, e)}
                            className="touch-target text-lg py-6"
                          >
                            <Info className="w-5 h-5" />
                          </Button>
                        </div>

                        {qtyInCart > 0 && (
                          <div className="flex items-center justify-center gap-2 p-2 bg-accent/10 rounded-lg">
                            <ShoppingCart className="w-4 h-4 text-accent" />
                            <span className="text-sm font-semibold text-accent">{qtyInCart} in cart</span>
                          </div>
                        )}
                      </div>
                    </CardContent>
                  </Card>
                )
              })}
            </div>
          </ScrollArea>
        </main>
      </div>

      <Dialog open={!!selectedProduct} onOpenChange={() => setSelectedProduct(null)}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle className="text-3xl">{selectedProduct?.name}</DialogTitle>
            <DialogDescription className="text-lg">
              {selectedProduct?.brand && `Brand: ${selectedProduct.brand}`}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-2xl font-bold">Price:</span>
              {selectedProduct &&
                (() => {
                  const { isDiscounted, effectivePrice } = getDiscountInfo(selectedProduct)
                  return isDiscounted ? (
                    <div className="text-right">
                      <p className="text-xl text-muted-foreground line-through">
                        ${selectedProduct.basePrice.toFixed(2)}
                      </p>
                      <p className="text-3xl font-bold text-destructive">${effectivePrice.toFixed(2)}</p>
                    </div>
                  ) : (
                    <p className="text-3xl font-bold text-accent">${selectedProduct.basePrice.toFixed(2)}</p>
                  )
                })()}
            </div>
            {selectedProduct && (
              <Button
                size="lg"
                onClick={() => {
                  handleQuickAdd(selectedProduct, { stopPropagation: () => {} } as React.MouseEvent)
                  setSelectedProduct(null)
                }}
                className="w-full text-xl py-6 bg-accent hover:bg-accent/90 text-accent-foreground"
              >
                <Plus className="w-5 h-5 mr-2" />
                Add to Cart
              </Button>
            )}
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}
