import type { LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'
import { cn } from '@/lib/cn'

export function StatCard({
  label,
  value,
  hint,
  icon: Icon,
}: {
  label: string
  value: string
  hint?: string
  icon?: LucideIcon
}) {
  return (
    <article className="rounded-card border border-line bg-surface p-4 shadow-card">
      <div className="flex items-start justify-between gap-3">
        <p className="text-label text-ink-secondary">{label}</p>
        {Icon ? (
          <span className="flex h-8 w-8 items-center justify-center rounded-control bg-green-soft text-green">
            <Icon size={16} aria-hidden="true" />
          </span>
        ) : null}
      </div>
      <p className="mt-3 text-title text-ink">{value}</p>
      {hint ? <p className="mt-1 text-meta text-ink-secondary">{hint}</p> : null}
    </article>
  )
}

export function ChartCard({
  title,
  description,
  empty,
  children,
}: {
  title: string
  description?: string
  empty?: boolean
  children?: ReactNode
}) {
  return (
    <section className="rounded-card border border-line bg-surface shadow-card">
      <header className="px-5 pb-2 pt-4">
        <h2 className="text-card text-ink">{title}</h2>
        {description ? <p className="mt-1 text-secondary text-ink-secondary">{description}</p> : null}
      </header>
      <div className={cn('px-5 pb-5', empty && 'pt-2')}>
        {empty ? (
          <p className="py-10 text-center text-secondary text-ink-secondary">No data for this period.</p>
        ) : (
          children
        )}
      </div>
    </section>
  )
}
