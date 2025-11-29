"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { CreditCard } from "lucide-react"

type CardFormProps = {
  onValidChange?: (isValid: boolean) => void
}

export function CardForm({ onValidChange }: CardFormProps) {
  const [cardholderName, setCardholderName] = useState("")
  const [cardNumber, setCardNumber] = useState("")
  const [expiry, setExpiry] = useState("")
  const [cvv, setCvv] = useState("")
  const [errors, setErrors] = useState<Record<string, string>>({})

  // Luhn algorithm for card validation
  const luhnCheck = (num: string): boolean => {
    const digits = num.replace(/\s/g, "").split("").reverse()
    let sum = 0
    for (let i = 0; i < digits.length; i++) {
      let digit = Number.parseInt(digits[i])
      if (i % 2 === 1) {
        digit *= 2
        if (digit > 9) digit -= 9
      }
      sum += digit
    }
    return sum % 10 === 0
  }

  const validateCardNumber = (value: string): boolean => {
    const cleaned = value.replace(/\s/g, "")
    if (cleaned.length !== 16) return false
    if (!/^\d+$/.test(cleaned)) return false
    return luhnCheck(cleaned)
  }

  const validateExpiry = (value: string): boolean => {
    if (!/^\d{2}\/\d{2}$/.test(value)) return false
    const [month, year] = value.split("/").map(Number)
    if (month < 1 || month > 12) return false
    const now = new Date()
    const currentYear = now.getFullYear() % 100
    const currentMonth = now.getMonth() + 1
    if (year < currentYear || (year === currentYear && month < currentMonth)) return false
    return true
  }

  const validateCvv = (value: string): boolean => {
    return /^\d{3,4}$/.test(value)
  }

  const isFormValid =
    cardholderName.trim().length > 0 &&
    validateCardNumber(cardNumber) &&
    validateExpiry(expiry) &&
    validateCvv(cvv)

  useEffect(() => {
    onValidChange?.(isFormValid)
  }, [isFormValid, onValidChange])

  const handleCardNumberChange = (value: string) => {
    // Remove non-digits
    const cleaned = value.replace(/\D/g, "")
    // Limit to 16 digits
    const limited = cleaned.slice(0, 16)
    // Format as #### #### #### ####
    const formatted = limited.match(/.{1,4}/g)?.join(" ") || limited
    setCardNumber(formatted)

    // Validate when exactly 16 digits are entered
    if (limited.length === 16) {
      if (!validateCardNumber(formatted)) {
        setErrors((prev) => ({ ...prev, cardNumber: "Invalid card number" }))
      } else {
        setErrors((prev) => {
          const { cardNumber, ...rest } = prev
          return rest
        })
      }
    } else if (limited.length > 0) {
      // Clear error while typing
      setErrors((prev) => {
        const { cardNumber, ...rest } = prev
        return rest
      })
    }
  }

  const handleExpiryChange = (value: string) => {
    // Remove non-digits
    const cleaned = value.replace(/\D/g, "")
    // Limit to 4 digits
    const limited = cleaned.slice(0, 4)
    // Format as MM/YY
    let formatted = limited
    if (limited.length >= 2) {
      formatted = `${limited.slice(0, 2)}/${limited.slice(2)}`
    }
    setExpiry(formatted)

    // Validate
    if (limited.length === 4) {
      if (!validateExpiry(formatted)) {
        setErrors((prev) => ({ ...prev, expiry: "Invalid or expired date" }))
      } else {
        setErrors((prev) => {
          const { expiry, ...rest } = prev
          return rest
        })
      }
    } else {
      setErrors((prev) => {
        const { expiry, ...rest } = prev
        return rest
      })
    }
  }

  const handleCvvChange = (value: string) => {
    // Remove non-digits
    const cleaned = value.replace(/\D/g, "")
    // Limit to 4 digits
    const limited = cleaned.slice(0, 4)
    setCvv(limited)

    // Validate
    if (limited.length >= 3) {
      if (!validateCvv(limited)) {
        setErrors((prev) => ({ ...prev, cvv: "Invalid CVV" }))
      } else {
        setErrors((prev) => {
          const { cvv, ...rest } = prev
          return rest
        })
      }
    } else {
      setErrors((prev) => {
        const { cvv, ...rest } = prev
        return rest
      })
    }
  }

  const handleNameChange = (value: string) => {
    setCardholderName(value)
  }

  return (
    <Card className="border-2 mt-6">
      <CardHeader className="bg-muted/50">
        <CardTitle className="text-2xl flex items-center gap-3">
          <CreditCard className="w-6 h-6 text-accent" />
          Card Details
        </CardTitle>
      </CardHeader>
      <CardContent className="p-6 space-y-6">
        {/* Cardholder Name */}
        <div className="space-y-2">
          <Label htmlFor="cardholder-name" className="text-xl font-semibold">
            Cardholder Name <span className="text-destructive">*</span>
          </Label>
          <Input
            id="cardholder-name"
            type="text"
            placeholder="John Doe"
            value={cardholderName}
            onChange={(e) => handleNameChange(e.target.value)}
            className="touch-target text-xl py-6 border-2"
            required
          />
        </div>

        {/* Card Number */}
        <div className="space-y-2">
          <Label htmlFor="card-number" className="text-xl font-semibold">
            Card Number <span className="text-destructive">*</span>
          </Label>
          <Input
            id="card-number"
            type="text"
            inputMode="numeric"
            placeholder="1234 5678 9012 3456"
            value={cardNumber}
            onChange={(e) => handleCardNumberChange(e.target.value)}
            className={`touch-target text-xl py-6 border-2 ${errors.cardNumber ? "border-destructive" : ""}`}
            required
          />
          {errors.cardNumber && <p className="text-destructive text-sm mt-1">{errors.cardNumber}</p>}
        </div>

        {/* Expiry and CVV */}
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="expiry" className="text-xl font-semibold">
              Expiry <span className="text-destructive">*</span>
            </Label>
            <Input
              id="expiry"
              type="text"
              inputMode="numeric"
              placeholder="MM/YY"
              value={expiry}
              onChange={(e) => handleExpiryChange(e.target.value)}
              className={`touch-target text-xl py-6 border-2 ${errors.expiry ? "border-destructive" : ""}`}
              required
            />
            {errors.expiry && <p className="text-destructive text-sm mt-1">{errors.expiry}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="cvv" className="text-xl font-semibold">
              CVV <span className="text-destructive">*</span>
            </Label>
            <Input
              id="cvv"
              type="text"
              inputMode="numeric"
              placeholder="123"
              value={cvv}
              onChange={(e) => handleCvvChange(e.target.value)}
              className={`touch-target text-xl py-6 border-2 ${errors.cvv ? "border-destructive" : ""}`}
              required
            />
            {errors.cvv && <p className="text-destructive text-sm mt-1">{errors.cvv}</p>}
          </div>
        </div>

        <div className="p-4 bg-muted/30 rounded-lg">
          <p className="text-sm text-muted-foreground">
            Your payment information is encrypted and secure. We never store your card details.
          </p>
        </div>
      </CardContent>
    </Card>
  )
}
