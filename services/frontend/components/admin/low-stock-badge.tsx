import { Badge } from "@/components/ui/badge"
import { AlertTriangle } from "lucide-react"

export function LowStockBadge({ stock, threshold }: { stock: number; threshold: number }) {
  const isCritical = stock <= threshold / 2

  return (
    <Badge className={`${isCritical ? "bg-destructive" : "bg-orange-500"} text-white text-sm px-3 py-1`}>
      <AlertTriangle className="w-4 h-4 mr-1" />
      {stock} left
    </Badge>
  )
}
