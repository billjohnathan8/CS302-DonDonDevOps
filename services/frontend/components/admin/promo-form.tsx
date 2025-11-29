"use client"

import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import type { Promotion } from "@/lib/types"

type PromoFormProps = {
  onSubmit: (promo: Omit<Promotion, "promoId">) => void
  onCancel: () => void
  initialPromotion?: Promotion | null
  mode?: "create" | "edit"
}

export function PromoForm({ onSubmit, onCancel, initialPromotion = null, mode = "create" }: PromoFormProps) {
  const [name, setName] = useState(initialPromotion?.name ?? "")
  const [discountPercent, setDiscountPercent] = useState(
    initialPromotion ? String(Math.round(initialPromotion.discountRate * 100)) : "",
  )
  const [startDateTime, setStartDateTime] = useState(formatInputDate(initialPromotion?.startDate))
  const [endDateTime, setEndDateTime] = useState(formatInputDate(initialPromotion?.endDate))

  useEffect(() => {
    if (initialPromotion) {
      setName(initialPromotion.name)
      setDiscountPercent(String(Math.round(initialPromotion.discountRate * 100)))
      setStartDateTime(formatInputDate(initialPromotion.startDate))
      setEndDateTime(formatInputDate(initialPromotion.endDate))
    } else if (mode === "create") {
      setName("")
      setDiscountPercent("")
      setStartDateTime("")
      setEndDateTime("")
    }
  }, [initialPromotion, mode])

  const handleSubmit = () => {
    if (!name || !discountPercent || !startDateTime || !endDateTime) return

    const discountValue = Number.parseFloat(discountPercent)
    if (!Number.isFinite(discountValue) || discountValue < 1 || discountValue > 100) return
    if (new Date(endDateTime) <= new Date(startDateTime)) return

    const startIso = toUtcIso(startDateTime)
    const endIso = toUtcIso(endDateTime)
    if (!startIso || !endIso) return

    onSubmit({
      name,
      discountRate: discountValue / 100,
      startDate: startIso,
      endDate: endIso,
    })

    if (mode === "create") {
      setName("")
      setDiscountPercent("")
      setStartDateTime("")
      setEndDateTime("")
    }
  }

  return (
    <div className="space-y-6 p-6 border-2 rounded-lg bg-card">
      <h3 className="text-2xl font-bold">{mode === "edit" ? "Edit Promotion" : "Create New Promotion"}</h3>

      <div className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="promo-name" className="text-lg">
            Promotion Name
          </Label>
          <Input
            id="promo-name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g., Weekend Special"
            className="text-lg h-12"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="promo-discount" className="text-lg">
            Discount Percentage
          </Label>
          <Input
            id="promo-discount"
            type="number"
            min="1"
            max="99"
            value={discountPercent}
            onChange={(e) => setDiscountPercent(e.target.value)}
            placeholder="e.g., 20"
            className="text-lg h-12"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="promo-start" className="text-lg">
              Start Time (UTC)
            </Label>
            <Input
              id="promo-start"
              type="datetime-local"
              value={startDateTime}
              onChange={(e) => setStartDateTime(e.target.value)}
              step="60"
              className="text-lg h-12"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="promo-end" className="text-lg">
              End Time (UTC)
            </Label>
            <Input
              id="promo-end"
              type="datetime-local"
              value={endDateTime}
              onChange={(e) => setEndDateTime(e.target.value)}
              step="60"
              className="text-lg h-12"
            />
          </div>
        </div>
      </div>

      <div className="flex gap-3">
        <Button variant="outline" size="lg" onClick={onCancel} className="flex-1 text-lg py-6 bg-transparent">
          Cancel
        </Button>
        <Button
          size="lg"
          onClick={handleSubmit}
          disabled={!isFormValid(name, discountPercent, startDateTime, endDateTime)}
          className="flex-1 text-lg py-6 bg-accent hover:bg-accent/90"
        >
          {mode === "edit" ? "Save Changes" : "Create Promotion"}
        </Button>
      </div>
    </div>
  )
}

function isFormValid(name: string, discountPercent: string, start: string, end: string) {
  if (!name || !discountPercent || !start || !end) return false
  const discountValue = Number.parseFloat(discountPercent)
  if (!Number.isFinite(discountValue) || discountValue < 1 || discountValue > 100) return false

  const startDate = parseInputDate(start)
  const endDate = parseInputDate(end)
  if (!startDate || !endDate) return false
  if (endDate <= startDate) return false
  return true
}

function toUtcIso(value: string) {
  const date = parseInputDate(value)
  return date?.toISOString()
}

function parseInputDate(value: string) {
  if (!value) return undefined
  const normalized = value.includes("T") ? value : value.replace(" ", "T")
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? undefined : date
}

function formatInputDate(value?: string) {
  if (!value) return ""
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ""
  const iso = date.toISOString()
  return iso.slice(0, 16)
}
