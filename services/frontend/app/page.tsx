"use client"

import { useRouter } from "next/navigation"
import { Card } from "@/components/ui/card"
import { Utensils, ShieldCheck, ArrowRight } from "lucide-react"

export default function LandingPage() {
  const router = useRouter()

  const options = [
    {
      title: "Our Menu",
      subtitle: "Browse and order your favorites",
      icon: Utensils,
      color: "text-accent",
      bgColor: "bg-accent/10",
      borderColor: "border-accent/20",
      hoverBg: "hover:bg-accent/20",
      path: "/menu",
    },
    {
      title: "Admin Portal",
      subtitle: "Manage inventory and orders",
      icon: ShieldCheck,
      color: "text-primary",
      bgColor: "bg-primary/10",
      borderColor: "border-primary/20",
      hoverBg: "hover:bg-primary/20",
      path: "/admin",
    },
  ]

  return (
    <div className="kiosk-mode min-h-screen bg-gradient-to-br from-background via-background to-secondary flex flex-col items-center justify-center p-8">
      <div className="flex flex-col items-center justify-center space-y-12 max-w-6xl mx-auto w-full">
        {/* Header */}
        <div className="text-center space-y-4">
          <h1 className="text-7xl font-bold text-foreground text-balance">Welcome</h1>
          <p className="text-3xl text-muted-foreground text-balance">Choose an option to continue</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 w-full max-w-5xl">
          {options.map((option) => {
            const Icon = option.icon
            return (
              <Card
                key={option.path}
                onClick={() => router.push(option.path)}
                className={`touch-target cursor-pointer transition-all duration-300 hover:scale-105 hover:shadow-2xl ${option.bgColor} ${option.borderColor} ${option.hoverBg} border-4 p-12 flex flex-col items-center justify-center space-y-8 min-h-[400px]`}
              >
                {/* Icon */}
                <div className={`bg-card rounded-full p-10 shadow-xl ${option.borderColor} border-2`}>
                  <Icon className={`w-24 h-24 ${option.color}`} strokeWidth={1.5} />
                </div>

                {/* Text */}
                <div className="text-center space-y-3">
                  <h2 className="text-5xl font-bold text-foreground">{option.title}</h2>
                  <p className="text-2xl text-muted-foreground">{option.subtitle}</p>
                </div>

                {/* Arrow indicator */}
                <ArrowRight className={`w-10 h-10 ${option.color} opacity-70`} />
              </Card>
            )
          })}
        </div>

        {/* Footer */}
        <div className="text-center text-muted-foreground text-lg mt-8">
          <p>Select an option to get started</p>
        </div>
      </div>
    </div>
  )
}
