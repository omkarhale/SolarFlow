import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { navigation } from '@/constants/navigation'
import { Sidebar } from '@/components/layout/Sidebar'
import { TopNavbar } from '@/components/layout/TopNavbar'
import { ResponsiveDrawer } from '@/components/layout/ResponsiveDrawer'

const STORAGE_KEY = 'solarflow.sidebar.collapsed'

export function AppLayout() {
  const location = useLocation()
  const [collapsed, setCollapsed] = useState(() => localStorage.getItem(STORAGE_KEY) === 'true')
  const [mobileOpen, setMobileOpen] = useState(false)
  const current = navigation.find((item) =>
    item.to === '/' ? location.pathname === '/' : location.pathname.startsWith(item.to),
  )

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, String(collapsed))
  }, [collapsed])

  useEffect(() => {
    setMobileOpen(false)
  }, [location.pathname])

  return (
    <div className="flex min-h-screen bg-canvas">
      <a
        href="#main"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-50 focus:rounded-control focus:bg-navy focus:px-3 focus:py-2 focus:text-[var(--sf-on-primary)]"
      >
        Skip to content
      </a>
      <Sidebar collapsed={collapsed} onToggle={() => setCollapsed((value) => !value)} />
      <ResponsiveDrawer open={mobileOpen} onClose={() => setMobileOpen(false)} />
      <div className="flex min-w-0 flex-1 flex-col">
        <TopNavbar onOpenMenu={() => setMobileOpen(true)} currentLabel={current?.label ?? 'Workspace'} />
        <main id="main" className="flex-1">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
