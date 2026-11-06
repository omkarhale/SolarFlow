import type { LucideIcon } from 'lucide-react'
import {
  ClipboardList,
  FolderKanban,
  LayoutDashboard,
  Package,
  Receipt,
  Settings,
  ShieldCheck,
  SunMedium,
  Users,
  Wallet,
  Wrench,
  BarChart3,
} from 'lucide-react'

export type NavItem = {
  label: string
  to: string
  icon: LucideIcon
  description: string
}

export const navigation: NavItem[] = [
  {
    label: 'Dashboard',
    to: '/',
    icon: LayoutDashboard,
    description: 'A daily view of revenue, pipeline, and installations.',
  },
  {
    label: 'Customers',
    to: '/customers',
    icon: Users,
    description: 'Households and the history of every installation.',
  },
  {
    label: 'Leads',
    to: '/leads',
    icon: SunMedium,
    description: 'Enquiries moving from first call to a signed quotation.',
  },
  {
    label: 'Site Surveys',
    to: '/surveys',
    icon: ClipboardList,
    description: 'Roof, load, and shading details captured on site.',
  },
  {
    label: 'Quotations',
    to: '/quotations',
    icon: Receipt,
    description: 'System size, pricing, tax, and a customer-ready quote.',
  },
  {
    label: 'Projects',
    to: '/projects',
    icon: FolderKanban,
    description: 'Installation progress, tasks, and site photos.',
  },
  {
    label: 'Payments',
    to: '/payments',
    icon: Wallet,
    description: 'Collections, outstanding balances, and receipts.',
  },
  {
    label: 'Inventory',
    to: '/inventory',
    icon: Package,
    description: 'Panels, inverters, and stock that is running low.',
  },
  {
    label: 'Service & Warranty',
    to: '/service',
    icon: Wrench,
    description: 'Warranty cover and service visits after commissioning.',
  },
  {
    label: 'Reports',
    to: '/reports',
    icon: BarChart3,
    description: 'Performance of the sales and installation teams.',
  },
  {
    label: 'Team',
    to: '/team',
    icon: ShieldCheck,
    description: 'People who sell, survey, and install.',
  },
  {
    label: 'Settings',
    to: '/settings',
    icon: Settings,
    description: 'Company profile and workspace preferences.',
  },
]
