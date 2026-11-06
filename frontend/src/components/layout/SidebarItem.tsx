import { NavLink } from 'react-router-dom'
import type { NavItem } from '@/constants/navigation'
import { cn } from '@/lib/cn'
import { Tooltip } from '@/components/common/navigation'

export function SidebarItem({ item, collapsed }: { item: NavItem; collapsed: boolean }) {
  const link = (
    <NavLink
      to={item.to}
      end={item.to === '/'}
      title={collapsed ? item.label : undefined}
      className={({ isActive }) =>
        cn(
          'flex h-10 items-center gap-3 rounded-control px-2.5 text-body text-ink-secondary transition-colors',
          collapsed && 'justify-center px-0',
          isActive ? 'bg-green-soft font-medium text-green-ink' : 'hover:bg-surface-muted hover:text-ink',
        )
      }
    >
      <item.icon size={18} aria-hidden="true" />
      {collapsed ? <span className="sr-only">{item.label}</span> : <span>{item.label}</span>}
    </NavLink>
  )

  if (!collapsed) return link
  return <Tooltip label={item.label}>{link}</Tooltip>
}
