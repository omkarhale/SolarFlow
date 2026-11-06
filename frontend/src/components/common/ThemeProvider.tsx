import { useLayoutEffect, useMemo, useState, type ReactNode } from 'react'
import { ThemeContext, type ThemeApi } from '@/components/common/theme-context'
import {
  applyTheme,
  deriveCustomTheme,
  persistTheme,
  readThemeSelection,
  type CustomThemeInput,
  type ThemeId,
} from '@/lib/theme'

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [selection, setSelection] = useState(readThemeSelection)

  useLayoutEffect(() => {
    applyTheme(selection)
  }, [selection])

  const api = useMemo<ThemeApi>(
    () => ({
      selection,
      selectPreset: (id: ThemeId) => {
        const next = { kind: 'preset' as const, id }
        persistTheme(next)
        setSelection(next)
      },
      applyCustom: (input: CustomThemeInput) => {
        const next = { kind: 'custom' as const, custom: input, vars: deriveCustomTheme(input) }
        persistTheme(next)
        setSelection(next)
      },
      reset: () => {
        const next = { kind: 'preset' as const, id: 'solar' as const }
        persistTheme(next)
        setSelection(next)
      },
    }),
    [selection],
  )

  return <ThemeContext.Provider value={api}>{children}</ThemeContext.Provider>
}
