"use client"

import { useEffect, useState, useCallback } from "react"
import { useRouter, usePathname } from "next/navigation"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { Progress } from "@/components/ui/progress"

const INACTIVITY_TIMEOUT = 60000 // 60 seconds
const WARNING_TIME = 15000 // Show warning 15 seconds before timeout

export function InactivityGuard() {
  const router = useRouter()
  const pathname = usePathname()
  const [lastActivity, setLastActivity] = useState(Date.now())
  const [showWarning, setShowWarning] = useState(false)
  const [countdown, setCountdown] = useState(15)

  // Don't show inactivity guard on home/attract screen or status board
  const isExemptPage = pathname === "/" || pathname === "/status"

  const resetActivity = useCallback(() => {
    setLastActivity(Date.now())
    setShowWarning(false)
    setCountdown(15)
  }, [])

  useEffect(() => {
    if (isExemptPage) return

    // Activity event listeners
    const events = ["mousedown", "mousemove", "keypress", "scroll", "touchstart", "click"]

    const handleActivity = () => {
      resetActivity()
    }

    events.forEach((event) => {
      document.addEventListener(event, handleActivity)
    })

    return () => {
      events.forEach((event) => {
        document.removeEventListener(event, handleActivity)
      })
    }
  }, [isExemptPage, resetActivity])

  useEffect(() => {
    if (isExemptPage) return

    const checkInactivity = setInterval(() => {
      const now = Date.now()
      const timeSinceActivity = now - lastActivity

      if (timeSinceActivity >= INACTIVITY_TIMEOUT) {
        // Timeout reached - return to home
        router.push("/")
        resetActivity()
      } else if (timeSinceActivity >= INACTIVITY_TIMEOUT - WARNING_TIME) {
        // Show warning
        setShowWarning(true)
        const remaining = Math.ceil((INACTIVITY_TIMEOUT - timeSinceActivity) / 1000)
        setCountdown(remaining)
      }
    }, 1000)

    return () => clearInterval(checkInactivity)
  }, [lastActivity, isExemptPage, router, resetActivity])

  if (isExemptPage || !showWarning) return null

  const progress = (countdown / 15) * 100

  return (
    <AlertDialog open={showWarning}>
      <AlertDialogContent className="max-w-2xl">
        <AlertDialogHeader>
          <AlertDialogTitle className="text-3xl">Are you still there?</AlertDialogTitle>
          <AlertDialogDescription className="text-xl">
            Your session will end in {countdown} seconds due to inactivity. Touch anywhere to continue.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <div className="py-4">
          <Progress value={progress} className="h-3" />
        </div>
        <AlertDialogFooter>
          <AlertDialogAction
            onClick={resetActivity}
            className="touch-target text-2xl px-12 py-6 bg-accent hover:bg-accent/90 text-accent-foreground"
          >
            I'm Still Here
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
