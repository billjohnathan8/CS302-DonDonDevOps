"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog"
import type { ProductAdmin } from "@/lib/types"

const CATEGORIES = [
  "Fruits & Vegetables",
  "Meat & Seafood",
  "Bakery",
  "Dairy & Eggs",
  "Pantry Staples",
  "Snacks",
  "Frozen Foods",
  "Beverages",
  "Household Essentials",
  "Baby & Kids",
  "Pet Supplies",
  "Home & Kitchen",
  "Seasonal & Festive",
  "Daily Necessities",
]

const formatDateTimeLocal = (value?: string | null) => {
  if (!value) return ""
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(value)) return value
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?Z$/.test(value)) return value.slice(0, 16)

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
  return offsetDate.toISOString().slice(0, 16)
}

type ProductFormProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  product?: ProductAdmin | null
  onSubmit: (data: Partial<ProductAdmin>) => void
}

export function ProductForm({ open, onOpenChange, product, onSubmit }: ProductFormProps) {
  const [formData, setFormData] = useState<Partial<ProductAdmin>>({
    name: "",
    priceSGD: 0,
    stock: 0,
    brand: "",
    category: "",
    expiryDate: "",
  })

  useEffect(() => {
    if (product) {
      setFormData({
        ProductID: product.ProductID,
        name: product.name,
        priceSGD: product.priceSGD,
        stock: product.stock,
        brand: product.brand ?? "",
        category: product.category ?? "",
        expiryDate: formatDateTimeLocal(product.expiryDate),
      })
    } else {
      setFormData({
        name: "",
        priceSGD: 0,
        stock: 0,
        brand: "",
        category: "",
        expiryDate: "",
      })
    }
  }, [product, open])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const normalizedExpiry = normalizeExpiryDate(formData.expiryDate)
    const payload = { ...formData, expiryDate: normalizedExpiry }
    onSubmit(payload)
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle className="text-2xl">{product ? "Edit Product" : "Create Product"}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="name" className="text-lg">
                Product Name
              </Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                required
                className="text-lg h-12"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="brand" className="text-lg">
                Brand
              </Label>
              <Input
                id="brand"
                value={formData.brand}
                onChange={(e) => setFormData({ ...formData, brand: e.target.value })}
                required
                className="text-lg h-12"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="priceSGD" className="text-lg">
                Price (SGD)
              </Label>
              <Input
                id="priceSGD"
                type="number"
                step="0.01"
                value={formData.priceSGD}
                onChange={(e) => setFormData({ ...formData, priceSGD: Number.parseFloat(e.target.value) })}
                required
                className="text-lg h-12"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="stock" className="text-lg">
                Stock Quantity
              </Label>
              <Input
                id="stock"
                type="number"
                value={formData.stock}
                onChange={(e) => setFormData({ ...formData, stock: Number.parseInt(e.target.value) })}
                required
                className="text-lg h-12"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="category" className="text-lg">
              Category
            </Label>
            <Select value={formData.category} onValueChange={(value) => setFormData({ ...formData, category: value })}>
              <SelectTrigger className="text-lg h-12">
                <SelectValue placeholder="Select category" />
              </SelectTrigger>
              <SelectContent>
                {CATEGORIES.map((cat) => (
                  <SelectItem key={cat} value={cat} className="text-lg">
                    {cat}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
            <Label htmlFor="expiryDate" className="text-lg">
              Expiry Date & Time
            </Label>
            <Input
              id="expiryDate"
              type="datetime-local"
              value={formData.expiryDate ?? ""}
              onChange={(e) => setFormData({ ...formData, expiryDate: e.target.value })}
              placeholder="2025-11-16T12:00"
              className="text-lg h-12"
            />
          </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} size="lg" className="text-lg">
              Cancel
            </Button>
            <Button type="submit" size="lg" className="text-lg bg-accent hover:bg-accent/90">
              {product ? "Update" : "Create"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

function normalizeExpiryDate(value?: string | null) {
  if (!value) return value ?? ""
  const trimmed = value.trim()
  if (!trimmed) return ""

  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$/.test(trimmed)) {
    return trimmed
  }

  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}Z$/.test(trimmed)) {
    return `${trimmed.slice(0, -1)}:00Z`
  }

  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+Z$/.test(trimmed)) {
    return trimmed.replace(/\.\d+Z$/, "Z")
  }

  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(trimmed)) {
    return `${trimmed}:00Z`
  }

  const date = new Date(trimmed)
  if (!Number.isNaN(date.getTime())) {
    const iso = date.toISOString()
    return `${iso.slice(0, 19)}Z`
  }

  return trimmed.endsWith("Z") ? trimmed.replace(/\.\d+Z$/, "Z") : `${trimmed}:00Z`
}
