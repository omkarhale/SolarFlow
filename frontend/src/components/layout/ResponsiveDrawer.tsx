import { useEffect, useRef } from 'react'
import { NavLink } from 'react-router-dom'
import { navigation } from '@/constants/navigation'

export function ResponsiveDrawer({ open, onClose }: { open: boolean; onClose: () => void }) {
  const ref = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const dialog = ref.current
    if (!dialog) return
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
  }, [open])

  return (
    <dialog
      ref={ref}
      className="m-0 h-full max-h-none w-[min(100%,18rem)] bg-transparent p-0 md:hidden"
      onClose={onClose}
      aria-label="Mobile navigation"
    >
      <div className="flex h-full flex-col bg-surface p-4">
        <div className="mb-4 flex items-center justify-between">
          <span className="text-card text-navy">SolarFlow</span>
          <button type="button" className="h-10 px-2 text-body" onClick={onClose}>
            Close
          </button>
        </div>
        <nav aria-label="Mobile" className="flex flex-col gap-1">
          {navigation.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              onClick={onClose}
              className={({ isActive }) =>
                `flex h-11 items-center rounded-control px-3 text-body ${isActive ? 'bg-green-soft font-medium text-green-ink' : 'text-ink'}`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </div>
    </dialog>
  )
}
