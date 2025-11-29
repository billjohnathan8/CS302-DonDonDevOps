import { Badge } from "@/components/ui/badge"
import { Clock } from "lucide-react"

export function ExpiryBadge({ daysUntilExpiry }: { daysUntilExpiry: number }) {
  const isCritical = daysUntilExpiry <= 2
  const isWarning = daysUntilExpiry <= 5

  return (
    <Badge
      className={`${
        isCritical ? "bg-destructive" : isWarning ? "bg-orange-500" : "bg-yellow-500"
      } text-white text-sm px-3 py-1`}
    >
      <Clock className="w-4 h-4 mr-1" />
      {daysUntilExpiry}d left
    </Badge>
  )
}
