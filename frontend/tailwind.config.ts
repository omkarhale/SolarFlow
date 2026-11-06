import type { Config } from 'tailwindcss'

const config: Config = {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        navy: 'var(--sf-primary)',
        green: {
          DEFAULT: 'var(--sf-accent)',
          soft: 'var(--sf-accent-soft)',
          ink: 'var(--sf-accent-ink)',
        },
        canvas: 'var(--sf-canvas)',
        surface: {
          DEFAULT: 'var(--sf-surface)',
          muted: 'var(--sf-surface-muted)',
        },
        ink: {
          DEFAULT: 'var(--sf-ink)',
          secondary: 'var(--sf-ink-secondary)',
          muted: 'var(--sf-ink-muted)',
        },
        line: 'var(--sf-line)',
        success: 'var(--sf-success)',
        warning: {
          DEFAULT: 'var(--sf-warning)',
          soft: 'var(--sf-warning-soft)',
          ink: 'var(--sf-warning-ink)',
        },
        danger: {
          DEFAULT: 'var(--sf-danger)',
          soft: 'var(--sf-danger-soft)',
          ink: 'var(--sf-danger-ink)',
        },
        info: {
          DEFAULT: 'var(--sf-info)',
          soft: 'var(--sf-info-soft)',
          ink: 'var(--sf-info-ink)',
        },
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      fontSize: {
        display: ['1.75rem', { lineHeight: '2.125rem', letterSpacing: '-0.025em', fontWeight: '600' }],
        title: ['1.125rem', { lineHeight: '1.5rem', letterSpacing: '-0.015em', fontWeight: '600' }],
        card: ['0.9375rem', { lineHeight: '1.375rem', fontWeight: '600' }],
        body: ['0.875rem', { lineHeight: '1.375rem', fontWeight: '400' }],
        secondary: ['0.8125rem', { lineHeight: '1.25rem', fontWeight: '400' }],
        label: ['0.75rem', { lineHeight: '1rem', letterSpacing: '0.01em', fontWeight: '500' }],
        meta: ['0.75rem', { lineHeight: '1rem', fontWeight: '400' }],
      },
      boxShadow: {
        card: '0 1px 2px rgba(20, 36, 58, 0.04)',
        pop: '0 12px 32px rgba(20, 36, 58, 0.08)',
      },
      borderRadius: {
        control: '8px',
        card: '12px',
      },
      spacing: {
        4.5: '1.125rem',
        18: '4.5rem',
      },
    },
  },
  plugins: [],
}

export default config
