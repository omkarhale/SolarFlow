import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { Button } from '@/components/ui/Button'
import { Sidebar } from '@/components/layout/Sidebar'
import { navigation } from '@/constants/navigation'
import { deriveCustomTheme, relativeLuminance, themePresets } from '@/lib/theme'
import { greetingFor } from '@/utils/datetime'

describe('design system', () => {
  it('renders an enabled primary button', () => {
    render(<Button>Save quotation</Button>)
    expect(screen.getByRole('button', { name: 'Save quotation' })).toBeEnabled()
  })

  it('exposes every primary destination', () => {
    render(
      <MemoryRouter>
        <Sidebar collapsed={false} onToggle={() => undefined} />
      </MemoryRouter>,
    )
    for (const item of navigation) {
      expect(screen.getByRole('link', { name: item.label })).toBeInTheDocument()
    }
  })

  it('greets by time of day', () => {
    expect(greetingFor(new Date('2026-11-06T08:00:00'))).toBe('Good morning')
    expect(greetingFor(new Date('2026-11-06T15:00:00'))).toBe('Good afternoon')
    expect(greetingFor(new Date('2026-11-06T20:00:00'))).toBe('Good evening')
  })

  it('offers twelve standard themes and keeps custom contrast readable', () => {
    expect(themePresets).toHaveLength(12)
    expect(themePresets.filter((preset) => preset.group === 'Dark')).toHaveLength(4)
    const dark = deriveCustomTheme({
      primary: '#14243A',
      accent: '#16A36A',
      canvas: '#F7F9FC',
      surface: '#FFFFFF',
      ink: '#182230',
    })
    const light = deriveCustomTheme({
      primary: '#F4F1EA',
      accent: '#16A36A',
      canvas: '#F7F9FC',
      surface: '#FFFFFF',
      ink: '#182230',
    })
    expect(dark['--sf-on-primary']).toBe('#ffffff')
    expect(light['--sf-on-primary']).toBe('#182230')
    const night = deriveCustomTheme({
      primary: '#1f8a5b',
      accent: '#8ee0b5',
      canvas: '#0e141c',
      surface: '#17202b',
      ink: '#e8eef6',
    })
    expect(night['--sf-warning-soft']).toBe('#3a2e18')
    expect(relativeLuminance('#14243A')).toBeLessThan(0.2)
  })
})
