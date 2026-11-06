import { createContext, useContext } from 'react'
import type { CustomThemeInput, ThemeId, ThemeSelection } from '@/lib/theme'

export type ThemeApi = {
  selection: ThemeSelection
  selectPreset: (id: ThemeId) => void
  applyCustom: (input: CustomThemeInput) => void
  reset: () => void
}

export const ThemeContext = createContext<ThemeApi | null>(null)

export function useTheme() {
  const context = useContext(ThemeContext)
  if (!context) throw new Error('useTheme must be used within ThemeProvider')
  return context
}
