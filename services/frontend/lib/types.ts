// Core menu types
export type MenuCategory = {
  id: string
  name: string
  sort: number
}

export type Product = {
  id: string
  name: string
  brand?: string
  basePrice: number // original (pre-discount) price
  categoryId: string
  flags?: ("popular" | "new")[]
  stock?: {
    qty: number
    lowStockThreshold?: number
  }
  // Discount fields
  promoPrice?: number // discounted price, if present
  discountPercent?: number // e.g., 20 means 20%
}

// Cart types
export type CartLine = {
  lineId: string
  productId: string
  name: string
  qty: number
  unitPrice: number // effective price after discount if any
  baseUnitPrice?: number // original price before discount
  promoName?: string
  note?: string
  lineTotal: number
}

export type CartTotals = {
  subtotal: number
  discounts: number // aggregated discount value (>= 0)
  total: number // subtotal - discounts
}

// Order types
export type OrderStatus = "placed" | "preparing" | "ready" | "completed"

export type Order = {
  id: string
  number: string
  origin: "kiosk"
  items: CartLine[]
  totals: CartTotals
  status: OrderStatus
  createdAt: string
  qrToken: string
}

// Admin-specific types for inventory, promotions, and restock orders
export type Promotion = {
  promoId: string
  name: string
  discountRate: number // 0..1 (e.g., 0.5 = 50%)
  startDate: string // ISO
  endDate: string // ISO
}

export type RestockOrder = {
  id: string
  supplier: string
  items: Array<{ productId: string; qty: number; productName?: string }>
  status: "draft" | "sent" | "received"
  createdAt: string
  expectedDate?: string
}

export type LowStockItem = {
  product: Product
  stock: number
  threshold: number
  lastUpdated: string
}

export type ExpiringItem = {
  product: Product
  expiresOn: string // ISO
  qty: number
  currentPrice: number
  daysUntilExpiry: number
}

// Admin-specific types for inventory, promotions, and restock orders
export type ProductAdmin = {
  ProductID: string // UUID
  name: string
  priceSGD: number
  stock: number
  brand: string
  category: string
  expiryDate?: string // ISO
  discountRate?: number // 0.5 = 50%
  img?: string
}

// API response types
export type MenuResponse = {
  categories: MenuCategory[]
  products: Product[]
}

export type PriceResponse = CartTotals

export type OrderResponse = Order

export type PaymentIntentResponse = {
  clientSecret?: string
  simulated: boolean
}

export type PaymentConfirmResponse = {
  success: boolean
  orderId: string
}

export type OrderStatusResponse = {
  status: OrderStatus
}

export type CardEntry = {
  name: string
  numberMasked: string // UI-only, formatted as #### #### #### ####
  expiry: string // "MM/YY"
  cvvMasked: string // UI-only
}
