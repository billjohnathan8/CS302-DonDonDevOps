"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Input } from "@/components/ui/input"
import { LowStockBadge } from "@/components/admin/low-stock-badge"
import { RestockDialog } from "@/components/admin/restock-dialog"
import { ProductForm } from "@/components/admin/product-form"
import { Package, RefreshCw, Plus, Pencil, Trash2, Search } from "lucide-react"
import type { LowStockItem, ProductAdmin } from "@/lib/types"
import { useToast } from "@/hooks/use-toast"

export default function InventoryPage() {
  const [lowStockItems, setLowStockItems] = useState<LowStockItem[]>([])
  const [allProducts, setAllProducts] = useState<ProductAdmin[]>([])
  const [filteredProducts, setFilteredProducts] = useState<ProductAdmin[]>([])
  const [searchQuery, setSearchQuery] = useState("")
  const [loading, setLoading] = useState(true)
  const [restockDialogOpen, setRestockDialogOpen] = useState(false)
  const [productFormOpen, setProductFormOpen] = useState(false)
  const [selectedProduct, setSelectedProduct] = useState<{ id: string; name: string } | null>(null)
  const [editingProduct, setEditingProduct] = useState<ProductAdmin | null>(null)
  const { toast } = useToast()

  useEffect(() => {
    fetchData()
  }, [])

  useEffect(() => {
    if (searchQuery.trim() === "") {
      setFilteredProducts(allProducts)
    } else {
      const query = searchQuery.toLowerCase()
      setFilteredProducts(
        allProducts.filter(
          (p) =>
            p.name.toLowerCase().includes(query) ||
            p.brand.toLowerCase().includes(query) ||
            p.category.toLowerCase().includes(query),
        ),
      )
    }
  }, [searchQuery, allProducts])

  const parseErrorResponse = async (response: Response) => {
    try {
      const data = await response.json()
      if (data?.message) return data.message
      if (data?.error) return data.error
      return response.statusText || "Inventory service request failed"
    } catch {
      return response.statusText || "Inventory service request failed"
    }
  }

  const fetchData = async () => {
    setLoading(true)
    try {
      const [lowStockRes, productsRes] = await Promise.all([fetch("/api/inventory/low-stock"), fetch("/api/products")])
      if (!lowStockRes.ok) throw new Error(await parseErrorResponse(lowStockRes))
      if (!productsRes.ok) throw new Error(await parseErrorResponse(productsRes))

      const [lowStock, products] = await Promise.all([lowStockRes.json(), productsRes.json()])
      setLowStockItems(lowStock)
      setAllProducts(products)
      setFilteredProducts(products)
    } catch (err) {
      const message = err instanceof Error ? err.message : "Unable to load inventory data."
      console.error("[DonDonDevOps] Failed to fetch inventory data:", err)
      toast({
        title: "Inventory unavailable",
        description: message,
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleRestock = (productId: string, productName: string) => {
    setSelectedProduct({ id: productId, name: productName })
    setRestockDialogOpen(true)
  }

  const handleRestockSubmit = async (data: { qty: number; expiryDate: string }) => {
    if (!selectedProduct) return

    try {
      const response = await fetch("/api/inventory/restock", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          productId: selectedProduct.id,
          quantity: data.qty,
          expiryDate: data.expiryDate,
        }),
      })

      if (!response.ok) {
        throw new Error(await parseErrorResponse(response))
      }

      toast({ title: "Success", description: "Stock updated successfully!" })
      fetchData()
    } catch (err) {
      console.error("[DonDonDevOps] Failed to create restock order:", err)
      toast({
        title: "Inventory update failed",
        description: err instanceof Error ? err.message : "Failed to restock product.",
        variant: "destructive",
      })
    }
  }

  const handleCreateProduct = () => {
    setEditingProduct(null)
    setProductFormOpen(true)
  }

  const handleEditProduct = (product: ProductAdmin) => {
    setEditingProduct(product)
    setProductFormOpen(true)
  }

  const handleDeleteProduct = async (productId: string) => {
    if (!confirm("Are you sure you want to delete this product?")) return

    try {
      const response = await fetch(`/api/products/${productId}`, { method: "DELETE" })
      if (!response.ok && response.status !== 204) {
        throw new Error(await parseErrorResponse(response))
      }
      toast({ title: "Success", description: "Product deleted successfully!" })
      fetchData()
    } catch (err) {
      console.error("[DonDonDevOps] Failed to delete product:", err)
      toast({
        title: "Error",
        description: err instanceof Error ? err.message : "Failed to delete product",
        variant: "destructive",
      })
    }
  }

  const handleProductSubmit = async (data: Partial<ProductAdmin>) => {
    try {
      if (editingProduct) {
        const response = await fetch(`/api/products/${editingProduct.ProductID}`, {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(data),
        })
        if (!response.ok) {
          throw new Error(await parseErrorResponse(response))
        }
        toast({ title: "Success", description: "Product updated successfully!" })
      } else {
        const response = await fetch("/api/products", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(data),
        })
        if (!response.ok) {
          throw new Error(await parseErrorResponse(response))
        }
        toast({ title: "Success", description: "Product created successfully!" })
      }
      fetchData()
    } catch (err) {
      console.error("[DonDonDevOps] Failed to save product:", err)
      toast({
        title: "Error",
        description: err instanceof Error ? err.message : "Failed to save product",
        variant: "destructive",
      })
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold">Inventory Management</h2>
          <p className="text-lg text-muted-foreground mt-1">Manage stock, products, and restock orders</p>
        </div>
        <Button size="lg" onClick={fetchData} variant="outline" className="text-lg px-6 py-6 bg-transparent">
          <RefreshCw className="w-5 h-5 mr-2" />
          Refresh
        </Button>
      </div>

      <Card className="border-2">
        <CardHeader>
          <CardTitle className="text-2xl">Low Stock Items</CardTitle>
        </CardHeader>
        <CardContent>
          <ScrollArea className="h-[400px]">
            {loading ? (
              <div className="text-center py-12">
                <Package className="w-12 h-12 animate-pulse mx-auto mb-4 text-accent" />
                <p className="text-xl text-muted-foreground">Loading inventory...</p>
              </div>
            ) : lowStockItems.length === 0 ? (
              <div className="text-center py-12">
                <Package className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
                <p className="text-xl text-muted-foreground">All items are well stocked!</p>
              </div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="text-lg">Product</TableHead>
                    <TableHead className="text-lg">Stock</TableHead>
                    <TableHead className="text-lg">Threshold</TableHead>
                    <TableHead className="text-lg">Last Updated</TableHead>
                    <TableHead className="text-lg">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {lowStockItems.map((item) => (
                    <TableRow key={item.product.id} className="hover:bg-muted/50">
                      <TableCell className="text-lg font-medium">{item.product.name}</TableCell>
                      <TableCell>
                        <LowStockBadge stock={item.stock} threshold={item.threshold} />
                      </TableCell>
                      <TableCell className="text-lg">{item.threshold}</TableCell>
                      <TableCell className="text-base text-muted-foreground">
                        {new Date(item.lastUpdated).toLocaleString()}
                      </TableCell>
                      <TableCell>
                        <Button
                          size="lg"
                          onClick={() => handleRestock(item.product.id, item.product.name)}
                          className="text-base px-6 py-4 bg-accent hover:bg-accent/90"
                        >
                          Restock
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </ScrollArea>
        </CardContent>
      </Card>

      <Card className="border-2">
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="text-2xl">Products</CardTitle>
          <Button size="lg" onClick={handleCreateProduct} className="text-lg px-6 py-6 bg-accent hover:bg-accent/90">
            <Plus className="w-5 h-5 mr-2" />
            Create Product
          </Button>
        </CardHeader>
        <CardContent>
          <div className="mb-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
              <Input
                placeholder="Search products..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10 text-lg h-12"
              />
            </div>
          </div>
          <ScrollArea className="h-[500px]">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="text-lg">Name</TableHead>
                  <TableHead className="text-lg">Price (SGD)</TableHead>
                  <TableHead className="text-lg">Stock</TableHead>
                  <TableHead className="text-lg">Brand</TableHead>
                  <TableHead className="text-lg">Category</TableHead>
                  <TableHead className="text-lg">Expiry</TableHead>
                  <TableHead className="text-lg">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredProducts.map((product) => (
                  <TableRow key={product.ProductID} className="hover:bg-muted/50">
                    <TableCell className="text-lg font-medium">{product.name}</TableCell>
                    <TableCell className="text-lg">${product.priceSGD.toFixed(2)}</TableCell>
                    <TableCell className="text-lg">{product.stock}</TableCell>
                    <TableCell className="text-base">{product.brand}</TableCell>
                    <TableCell className="text-base">{product.category}</TableCell>
                    <TableCell className="text-base text-muted-foreground">
                      {product.expiryDate ? new Date(product.expiryDate).toLocaleDateString() : "N/A"}
                    </TableCell>
                    <TableCell>
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleEditProduct(product)}
                          className="text-base px-4 py-2"
                        >
                          <Pencil className="w-4 h-4" />
                        </Button>
                        <Button
                          size="sm"
                          variant="destructive"
                          onClick={() => handleDeleteProduct(product.ProductID)}
                          className="text-base px-4 py-2"
                        >
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </ScrollArea>
        </CardContent>
      </Card>

      {selectedProduct && (
        <RestockDialog
          open={restockDialogOpen}
          onOpenChange={setRestockDialogOpen}
          productName={selectedProduct.name}
          onSubmit={handleRestockSubmit}
        />
      )}

      <ProductForm
        open={productFormOpen}
        onOpenChange={setProductFormOpen}
        product={editingProduct}
        onSubmit={handleProductSubmit}
      />
    </div>
  )
}
