"use client"

import type React from "react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarInset,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
  SidebarTrigger,
} from "@/components/ui/sidebar"
import { Button } from "@/components/ui/button"
import { Package, Clock, Tag, ShoppingBag, LogOut, Store } from "lucide-react"

const menuItems = [
  { title: "Inventory", icon: Package, href: "/admin/inventory" },
  { title: "Expiring Items", icon: Clock, href: "/admin/expiring" },
  { title: "Promotions", icon: Tag, href: "/admin/promotions" },
  { title: "Order History", icon: ShoppingBag, href: "/admin/orders" },
]

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname()

  return (
    <SidebarProvider>
      <Sidebar>
        <SidebarHeader className="border-b-2 border-border p-6">
          <div className="flex items-center gap-3">
            <Store className="w-8 h-8 text-accent" />
            <div>
              <h2 className="text-2xl font-bold text-foreground">Admin Panel</h2>
              <p className="text-sm text-muted-foreground">Management System</p>
            </div>
          </div>
        </SidebarHeader>

        <SidebarContent>
          <SidebarGroup>
            <SidebarGroupLabel className="text-lg">Navigation</SidebarGroupLabel>
            <SidebarGroupContent>
              <SidebarMenu>
                {menuItems.map((item) => (
                  <SidebarMenuItem key={item.href}>
                    <SidebarMenuButton asChild size="lg" isActive={pathname === item.href} className="text-lg">
                      <Link href={item.href}>
                        <item.icon className="w-5 h-5" />
                        <span>{item.title}</span>
                      </Link>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                ))}
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        </SidebarContent>

        <SidebarFooter className="border-t-2 border-border p-4">
          <Button variant="outline" size="lg" onClick={() => (window.location.href = "/")} className="w-full text-lg">
            <LogOut className="w-5 h-5 mr-2" />
            Exit Admin
          </Button>
        </SidebarFooter>
      </Sidebar>

      <SidebarInset>
        <header className="sticky top-0 z-10 flex h-16 items-center gap-4 border-b-2 border-border bg-card px-6 shadow-sm">
          <SidebarTrigger />
          <h1 className="text-2xl font-bold text-foreground">
            {menuItems.find((item) => item.href === pathname)?.title || "Admin"}
          </h1>
        </header>
        <main className="flex-1 p-6">{children}</main>
      </SidebarInset>
    </SidebarProvider>
  )
}
