import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/Button'
import { useTheme } from '@/components/common/theme-context'
import { cn } from '@/lib/cn'
import { defaultCustomTheme, themePresets, type CustomThemeInput } from '@/lib/theme'

const fields: { key: keyof CustomThemeInput; label: string }[] = [
  { key: 'primary', label: 'Primary' },
  { key: 'accent', label: 'Accent' },
  { key: 'canvas', label: 'Background' },
  { key: 'surface', label: 'Surface' },
  { key: 'ink', label: 'Text' },
]

export function ThemeSettings() {
  const theme = useTheme()
  const [draft, setDraft] = useState<CustomThemeInput>(
    theme.selection.kind === 'custom' ? theme.selection.custom : defaultCustomTheme,
  )

  useEffect(() => {
    setDraft(theme.selection.kind === 'custom' ? theme.selection.custom : defaultCustomTheme)
  }, [theme.selection])

  function updateCustom(key: keyof CustomThemeInput, value: string) {
    const next = { ...draft, [key]: value }
    setDraft(next)
    theme.applyCustom(next)
  }

  return (
    <section id="appearance" className="flex flex-col gap-4">
      <div>
        <h2 className="text-title text-ink">Appearance</h2>
        <p className="mt-1 max-w-2xl text-body text-ink-secondary">
          Choose a light or dark workspace, or set your own primary, accent, and surface colors.
          The choice stays on this browser.
        </p>
      </div>

      <fieldset className="rounded-card border border-line bg-surface p-4 shadow-card">
        <legend className="px-1 text-card text-ink">Standard themes</legend>
        <div className="mt-3 flex flex-col gap-4">
          {(['Light', 'Dark'] as const).map((group) => (
            <div key={group}>
              <p className="mb-2 text-label text-ink-secondary">{group}</p>
              <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                {themePresets
                  .filter((preset) => preset.group === group)
                  .map((preset) => {
                    const selected = theme.selection.kind === 'preset' && theme.selection.id === preset.id
                    return (
                      <label
                        key={preset.id}
                        className={cn(
                          'cursor-pointer rounded-card border p-3 transition-colors',
                          selected ? 'border-navy bg-green-soft' : 'border-line hover:bg-surface-muted',
                        )}
                      >
                        <input
                          type="radio"
                          name="standard-theme"
                          className="sr-only"
                          checked={selected}
                          onChange={() => theme.selectPreset(preset.id)}
                        />
                        <span className="flex gap-1.5" aria-hidden="true">
                          <span className="h-6 w-6 rounded-control" style={{ background: preset.swatch.primary }} />
                          <span className="h-6 w-6 rounded-control" style={{ background: preset.swatch.accent }} />
                          <span className="h-6 w-6 rounded-control border border-line" style={{ background: preset.swatch.canvas }} />
                        </span>
                        <span className="mt-3 block text-body font-medium text-ink">{preset.name}</span>
                        <span className="mt-1 block text-meta text-ink-secondary">{preset.description}</span>
                      </label>
                    )
                  })}
              </div>
            </div>
          ))}
        </div>
      </fieldset>

      <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_18rem]">
        <fieldset className="rounded-card border border-line bg-surface p-4 shadow-card">
          <legend className="px-1 text-card text-ink">Custom theme</legend>
          <p className="mt-2 text-secondary text-ink-secondary">
            Dark primary colors keep white button text readable. A light primary switches the label to dark text.
          </p>
          <div className="mt-4 grid gap-3 sm:grid-cols-2">
            {fields.map((field) => (
              <label key={field.key} className="flex items-center justify-between gap-3 rounded-control border border-line px-3 py-2">
                <span className="text-body text-ink">{field.label}</span>
                <input
                  type="color"
                  aria-label={field.label}
                  value={draft[field.key]}
                  className="h-9 w-12 cursor-pointer bg-transparent"
                  onChange={(event) => updateCustom(field.key, event.target.value)}
                />
              </label>
            ))}
          </div>
          <div className="mt-4">
            <Button variant="secondary" onClick={theme.reset}>
              Reset to Solar Navy
            </Button>
          </div>
        </fieldset>

        <div className="rounded-card border border-line bg-canvas p-4 shadow-card">
          <p className="text-label text-ink-secondary">Preview</p>
          <p className="mt-3 text-card text-ink">Installation workspace</p>
          <p className="mt-1 text-secondary text-ink-secondary">Buttons, status, and surfaces follow the active theme.</p>
          <div className="mt-4 flex flex-wrap gap-2">
            <Button>Save</Button>
            <span className="inline-flex h-8 items-center rounded-full bg-green-soft px-2.5 text-meta font-medium text-green-ink">
              Active
            </span>
          </div>
        </div>
      </div>
    </section>
  )
}
