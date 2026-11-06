import { useEffect, useId, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Palette } from 'lucide-react'
import { useTheme } from '@/components/common/theme-context'
import { cn } from '@/lib/cn'
import { themePresets } from '@/lib/theme'

export function ThemeMenu() {
  const theme = useTheme()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)
  const menuId = useId()

  useEffect(() => {
    function onPointer(event: MouseEvent) {
      if (!ref.current?.contains(event.target as Node)) setOpen(false)
    }
    function onKey(event: KeyboardEvent) {
      if (event.key === 'Escape') setOpen(false)
    }
    document.addEventListener('mousedown', onPointer)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onPointer)
      document.removeEventListener('keydown', onKey)
    }
  }, [])

  return (
    <div ref={ref} className="relative">
      <button
        type="button"
        className="inline-flex h-10 w-10 items-center justify-center rounded-control text-ink-secondary hover:bg-surface"
        aria-label="Choose theme"
        aria-haspopup="menu"
        aria-expanded={open}
        aria-controls={menuId}
        onClick={() => setOpen((current) => !current)}
      >
        <Palette size={18} />
      </button>
      {open ? (
        <div id={menuId} role="menu" className="absolute right-0 z-30 mt-1 max-h-96 w-60 overflow-y-auto rounded-card border border-line bg-surface p-1 shadow-pop">
          {(['Light', 'Dark'] as const).map((group) => (
            <div key={group} className="py-1">
              <p className="px-2 py-1 text-label text-ink-muted">{group}</p>
              {themePresets
                .filter((preset) => preset.group === group)
                .map((preset) => {
                  const selected = theme.selection.kind === 'preset' && theme.selection.id === preset.id
                  return (
                    <button
                      key={preset.id}
                      type="button"
                      role="menuitemradio"
                      aria-checked={selected}
                      className={cn(
                        'flex h-9 w-full items-center gap-2 rounded-control px-2 text-left text-body',
                        selected ? 'bg-green-soft text-green-ink' : 'text-ink hover:bg-surface-muted',
                      )}
                      onClick={() => {
                        theme.selectPreset(preset.id)
                        setOpen(false)
                      }}
                    >
                      <span className="h-3 w-3 rounded-full border border-line" style={{ background: preset.swatch.canvas }} aria-hidden="true" />
                      {preset.name}
                    </button>
                  )
                })}
            </div>
          ))}
          <button
            type="button"
            role="menuitem"
            className="flex h-9 w-full items-center rounded-control px-2 text-left text-body text-ink hover:bg-surface-muted"
            onClick={() => {
              setOpen(false)
              navigate('/settings#appearance')
            }}
          >
            Custom theme
          </button>
        </div>
      ) : null}
    </div>
  )
}
