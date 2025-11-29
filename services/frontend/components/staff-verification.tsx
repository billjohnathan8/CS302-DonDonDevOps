"use client"

import type React from "react"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Lock, X } from "lucide-react"

type StaffVerificationProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  onVerified: () => void
  title?: string
  description?: string
}

// Simple PIN verification (in production, this would be more secure)
const STAFF_PIN = "1234"

export function StaffVerification({
  open,
  onOpenChange,
  onVerified,
  title = "Staff Verification Required",
  description = "Please enter the staff PIN to continue",
}: StaffVerificationProps) {
  const [pin, setPin] = useState("")
  const [error, setError] = useState("")

  const handleVerify = () => {
    if (pin === STAFF_PIN) {
      setError("")
      setPin("")
      onVerified()
      onOpenChange(false)
    } else {
      setError("Incorrect PIN. Please try again.")
      setPin("")
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      handleVerify()
    }
  }

  const handleCancel = () => {
    setPin("")
    setError("")
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle className="text-3xl flex items-center gap-3">
            <Lock className="w-8 h-8 text-accent" />
            {title}
          </DialogTitle>
          <DialogDescription className="text-xl">{description}</DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="staff-pin" className="text-xl">
              Staff PIN
            </Label>
            <Input
              id="staff-pin"
              type="password"
              inputMode="numeric"
              maxLength={4}
              value={pin}
              onChange={(e) => {
                setPin(e.target.value.replace(/\D/g, ""))
                setError("")
              }}
              onKeyPress={handleKeyPress}
              placeholder="Enter 4-digit PIN"
              className="text-2xl text-center tracking-widest h-16"
              autoFocus
            />
          </div>

          {error && <p className="text-lg text-destructive text-center">{error}</p>}

          {/* Number Pad */}
          <div className="grid grid-cols-3 gap-3">
            {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
              <Button
                key={num}
                variant="outline"
                size="lg"
                onClick={() => {
                  if (pin.length < 4) {
                    setPin(pin + num)
                    setError("")
                  }
                }}
                className="touch-target text-3xl h-20 font-bold"
              >
                {num}
              </Button>
            ))}
            <Button
              variant="outline"
              size="lg"
              onClick={() => setPin(pin.slice(0, -1))}
              className="touch-target text-2xl h-20"
            >
              <X className="w-6 h-6" />
            </Button>
            <Button
              variant="outline"
              size="lg"
              onClick={() => {
                if (pin.length < 4) {
                  setPin(pin + "0")
                  setError("")
                }
              }}
              className="touch-target text-3xl h-20 font-bold"
            >
              0
            </Button>
            <Button variant="outline" size="lg" onClick={() => setPin("")} className="touch-target text-xl h-20">
              Clear
            </Button>
          </div>
        </div>

        <DialogFooter className="gap-3">
          <Button
            variant="outline"
            size="lg"
            onClick={handleCancel}
            className="touch-target text-xl px-8 py-6 bg-transparent"
          >
            Cancel
          </Button>
          <Button
            size="lg"
            onClick={handleVerify}
            disabled={pin.length !== 4}
            className="touch-target text-xl px-8 py-6 bg-accent hover:bg-accent/90 text-accent-foreground"
          >
            Verify
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
