import { Badge } from "@/components/ui/badge"
import { Percent } from "lucide-react"

export function DiscountBadge({ discountRate }: { discountRate: number }) {
  const percent = Math.round(discountRate * 100)

  return (
    <Badge className="bg-green-500 text-white text-sm px-3 py-1">
      <Percent className="w-4 h-4 mr-1" />
      {percent}% OFF
    </Badge>
  )
}
