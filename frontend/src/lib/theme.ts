export const THEME_STORAGE_KEY = 'solarflow.theme'

export const themeIds = [
  'solar',
  'harbor',
  'dune',
  'slate',
  'indigo',
  'evergreen',
  'aurora',
  'lagoon',
  'midnight',
  'obsidian',
  'dusk',
  'ember',
] as const

export type ThemeId = (typeof themeIds)[number]

export type CustomThemeInput = {
  primary: string
  accent: string
  canvas: string
  surface: string
  ink: string
}

export type ThemeSelection =
  | { kind: 'preset'; id: ThemeId }
  | { kind: 'custom'; custom: CustomThemeInput; vars: Record<string, string> }

export type ThemePreset = {
  id: ThemeId
  name: string
  description: string
  group: 'Light' | 'Dark'
  swatch: {
    primary: string
    accent: string
    canvas: string
  }
}

export const themePresets: ThemePreset[] = [
  {
    id: 'solar',
    name: 'Solar Navy',
    description: 'Deep navy with a quiet solar green. The default workspace.',
    group: 'Light',
    swatch: { primary: '#14243A', accent: '#16A36A', canvas: '#F7F9FC' },
  },
  {
    id: 'harbor',
    name: 'Harbor',
    description: 'Coastal blue and teal for a cooler operations desk.',
    group: 'Light',
    swatch: { primary: '#0E2438', accent: '#0F7A78', canvas: '#F4F7F8' },
  },
  {
    id: 'dune',
    name: 'Dune',
    description: 'Warm stone and copper, still restrained enough for daily work.',
    group: 'Light',
    swatch: { primary: '#3A3128', accent: '#B86A2D', canvas: '#F8F5F0' },
  },
  {
    id: 'slate',
    name: 'Slate',
    description: 'Graphite structure with a moss accent.',
    group: 'Light',
    swatch: { primary: '#243038', accent: '#3E7C59', canvas: '#F5F6F7' },
  },
  {
    id: 'indigo',
    name: 'Indigo Desk',
    description: 'Ink blue and indigo, closer to a product studio.',
    group: 'Light',
    swatch: { primary: '#1C2340', accent: '#4C51C6', canvas: '#F6F7FB' },
  },
  {
    id: 'evergreen',
    name: 'Evergreen',
    description: 'Forest primary with a clear green accent.',
    group: 'Light',
    swatch: { primary: '#143028', accent: '#1F8A5B', canvas: '#F4F7F5' },
  },
  {
    id: 'aurora',
    name: 'Aurora',
    description: 'Soft daylight with a warm gold accent.',
    group: 'Light',
    swatch: { primary: '#243044', accent: '#C4A15A', canvas: '#F7F4EC' },
  },
  {
    id: 'lagoon',
    name: 'Lagoon',
    description: 'Clear water blue with a bright sea accent.',
    group: 'Light',
    swatch: { primary: '#123044', accent: '#1287A8', canvas: '#F3F8FA' },
  },
  {
    id: 'midnight',
    name: 'Midnight',
    description: 'Night navy with a luminous solar green.',
    group: 'Dark',
    swatch: { primary: '#1F8A5B', accent: '#8EE0B5', canvas: '#0E141C' },
  },
  {
    id: 'obsidian',
    name: 'Obsidian',
    description: 'Graphite night desk with a cool mint accent.',
    group: 'Dark',
    swatch: { primary: '#3DDC97', accent: '#9EE7C4', canvas: '#121417' },
  },
  {
    id: 'dusk',
    name: 'Dusk',
    description: 'Indigo evening with a violet highlight.',
    group: 'Dark',
    swatch: { primary: '#7C83F6', accent: '#C4C7FF', canvas: '#12141F' },
  },
  {
    id: 'ember',
    name: 'Ember',
    description: 'Warm charcoal with copper light.',
    group: 'Dark',
    swatch: { primary: '#E0A15A', accent: '#F0C48A', canvas: '#161311' },
  },
]

export const defaultCustomTheme: CustomThemeInput = {
  primary: '#14243A',
  accent: '#16A36A',
  canvas: '#F7F9FC',
  surface: '#FFFFFF',
  ink: '#182230',
}

const brandVars = [
  '--sf-primary',
  '--sf-primary-hover',
  '--sf-on-primary',
  '--sf-accent',
  '--sf-accent-soft',
  '--sf-accent-ink',
  '--sf-canvas',
  '--sf-surface',
  '--sf-surface-muted',
  '--sf-ink',
  '--sf-ink-secondary',
  '--sf-ink-muted',
  '--sf-line',
  '--sf-warning-soft',
  '--sf-warning-ink',
  '--sf-danger-soft',
  '--sf-danger-ink',
  '--sf-info-soft',
  '--sf-info-ink',
] as const

function channel(hex: string, start: number) {
  return Number.parseInt(hex.slice(start, start + 2), 16)
}

function clamp(value: number) {
  return Math.max(0, Math.min(255, Math.round(value)))
}

function toHex(red: number, green: number, blue: number) {
  return `#${[red, green, blue].map((part) => clamp(part).toString(16).padStart(2, '0')).join('')}`
}

function normalizeHex(value: string) {
  const hex = value.trim().toLowerCase()
  if (/^#[0-9a-f]{6}$/.test(hex)) return hex
  return '#14243a'
}

function mix(source: string, target: string, targetWeight: number) {
  const from = normalizeHex(source)
  const to = normalizeHex(target)
  const keep = 1 - targetWeight
  return toHex(
    channel(from, 1) * keep + channel(to, 1) * targetWeight,
    channel(from, 3) * keep + channel(to, 3) * targetWeight,
    channel(from, 5) * keep + channel(to, 5) * targetWeight,
  )
}

export function relativeLuminance(hex: string) {
  const color = normalizeHex(hex)
  const channels = [1, 3, 5].map((start) => {
    const value = channel(color, start) / 255
    return value <= 0.03928 ? value / 12.92 : ((value + 0.055) / 1.055) ** 2.4
  })
  return 0.2126 * channels[0] + 0.7152 * channels[1] + 0.0722 * channels[2]
}

export function deriveCustomTheme(input: CustomThemeInput) {
  const primary = normalizeHex(input.primary)
  const accent = normalizeHex(input.accent)
  const canvas = normalizeHex(input.canvas)
  const surface = normalizeHex(input.surface)
  const ink = normalizeHex(input.ink)
  const dark = relativeLuminance(canvas) < 0.2
  return {
    '--sf-primary': primary,
    '--sf-primary-hover': relativeLuminance(primary) < 0.25 ? mix(primary, '#ffffff', 0.14) : mix(primary, '#000000', 0.14),
    '--sf-on-primary': relativeLuminance(primary) > 0.45 ? ink : '#ffffff',
    '--sf-accent': accent,
    '--sf-accent-soft': dark ? mix(accent, surface, 0.78) : mix(accent, '#ffffff', 0.86),
    '--sf-accent-ink': dark ? mix(accent, '#ffffff', 0.42) : mix(accent, '#000000', 0.28),
    '--sf-canvas': canvas,
    '--sf-surface': surface,
    '--sf-surface-muted': mix(canvas, ink, dark ? 0.08 : 0.045),
    '--sf-ink': ink,
    '--sf-ink-secondary': mix(ink, canvas, dark ? 0.28 : 0.46),
    '--sf-ink-muted': mix(ink, canvas, dark ? 0.48 : 0.64),
    '--sf-line': mix(ink, canvas, dark ? 0.78 : 0.86),
    '--sf-warning-soft': dark ? '#3a2e18' : '#fff6e8',
    '--sf-warning-ink': dark ? '#f0c27a' : '#9a6700',
    '--sf-danger-soft': dark ? '#3a1e22' : '#fdecec',
    '--sf-danger-ink': dark ? '#f0a0a0' : '#b42318',
    '--sf-info-soft': dark ? '#1a2c42' : '#eaf2fc',
    '--sf-info-ink': dark ? '#9ec4f0' : '#1d4e89',
  }
}

export function readThemeSelection(): ThemeSelection {
  try {
    const raw = localStorage.getItem(THEME_STORAGE_KEY)
    if (!raw) return { kind: 'preset', id: 'solar' }
    const parsed = JSON.parse(raw) as ThemeSelection
    if (parsed.kind === 'preset' && themeIds.includes(parsed.id)) return parsed
    if (parsed.kind === 'custom' && parsed.custom && parsed.vars) return parsed
  } catch {
    return { kind: 'preset', id: 'solar' }
  }
  return { kind: 'preset', id: 'solar' }
}

export function applyTheme(selection: ThemeSelection) {
  const root = document.documentElement
  for (const name of brandVars) root.style.removeProperty(name)
  if (selection.kind === 'preset') {
    root.dataset.theme = selection.id
    return
  }
  root.dataset.theme = 'custom'
  for (const [name, value] of Object.entries(selection.vars)) {
    root.style.setProperty(name, value)
  }
}

export function persistTheme(selection: ThemeSelection) {
  localStorage.setItem(THEME_STORAGE_KEY, JSON.stringify(selection))
  applyTheme(selection)
}
