import { PanelLeftClose, PanelLeftOpen } from 'lucide-react'
import { navigation } from '@/constants/navigation'
import { SidebarItem } from '@/components/layout/SidebarItem'

function Mark() {
  return (
    <svg width="32" height="32" viewBox="0 0 32 32" aria-hidden="true">
      <rect width="32" height="32" rx="8" fill="var(--sf-primary)" />
      <path d="M7 19.5 16 12l9 7.5" fill="none" stroke="var(--sf-accent)" strokeWidth="1.8" strokeLinejoin="round" />
      <rect x="10" y="19" width="12" height="6" rx="1" fill="var(--sf-accent)" />
      <circle cx="23" cy="10" r="2" fill="var(--sf-accent)" />
    </svg>
  )
}

export function Sidebar({
  collapsed,
  onToggle,
}: {
  collapsed: boolean
  onToggle: () => void
}) {
  return (
    <aside
      className={`hidden h-screen shrink-0 flex-col border-r border-line bg-surface transition-[width] duration-200 ease-out md:flex ${collapsed ? 'w-[72px]' : 'w-64'}`}
    >
      <div className={`flex h-16 items-center ${collapsed ? 'justify-center' : 'justify-between px-4'}`}>
        <div className="flex items-center gap-2.5">
          <Mark />
          {collapsed ? null : <span className="text-card text-navy">SolarFlow</span>}
        </div>
      </div>
      <nav aria-label="Primary" className="flex flex-1 flex-col gap-1 overflow-y-auto px-3 pb-3">
        {navigation.map((item) => (
          <SidebarItem key={item.to} item={item} collapsed={collapsed} />
        ))}
      </nav>
      <div className="border-t border-line p-3">
        <button
          type="button"
          onClick={onToggle}
          className="flex h-10 w-full items-center justify-center gap-2 rounded-control text-secondary text-ink-secondary hover:bg-surface-muted"
          aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {collapsed ? <PanelLeftOpen size={18} /> : <PanelLeftClose size={18} />}
          {collapsed ? null : <span>Collapse</span>}
        </button>
      </div>
    </aside>
  )
}
