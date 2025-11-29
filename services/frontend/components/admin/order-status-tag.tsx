import { Badge } from "@/components/ui/badge"
import type { OrderStatus } from "@/lib/types"

export function OrderStatusTag({ status }: { status: OrderStatus }) {
  const config = {
    pending: { label: "Pending", color: "bg-yellow-100 text-yellow-600" },
    placed: { label: "Placed", color: "bg-blue-100 text-blue-600" },
    preparing: { label: "Preparing", color: "bg-accent/10 text-accent" },
    ready: { label: "Ready", color: "bg-green-100 text-green-600" },
    completed: { label: "Completed", color: "bg-gray-100 text-gray-600" },
  }

  const { label, color } = config[status]

  return <Badge className={`${color} text-base px-4 py-1`}>{label}</Badge>
}
