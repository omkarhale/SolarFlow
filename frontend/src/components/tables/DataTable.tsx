import type { ReactNode } from 'react'
import { cn } from '@/lib/cn'

export type Column<T> = {
  id: string
  header: string
  cell: (row: T) => ReactNode
  className?: string
}

export function DataTable<T>({
  columns,
  rows,
  getRowId,
  empty,
}: {
  columns: Column<T>[]
  rows: T[]
  getRowId: (row: T) => string
  empty: ReactNode
}) {
  if (rows.length === 0) return <>{empty}</>
  return (
    <div className="overflow-x-auto rounded-card border border-line bg-surface">
      <table className="min-w-full border-collapse text-left">
        <thead className="bg-surface-muted">
          <tr>
            {columns.map((column) => (
              <th
                key={column.id}
                scope="col"
                className={cn('px-4 py-3 text-label text-ink-secondary', column.className)}
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={getRowId(row)} className="border-t border-line">
              {columns.map((column) => (
                <td key={column.id} className={cn('px-4 py-3 text-body text-ink', column.className)}>
                  {column.cell(row)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export function Pagination({
  page,
  pageCount,
  onPageChange,
}: {
  page: number
  pageCount: number
  onPageChange: (page: number) => void
}) {
  return (
    <nav className="flex items-center justify-between gap-3" aria-label="Pagination">
      <p className="text-meta text-ink-secondary">
        Page {page} of {Math.max(pageCount, 1)}
      </p>
      <div className="flex gap-2">
        <button
          type="button"
          className="h-8 rounded-control border border-line bg-surface px-3 text-secondary disabled:opacity-40"
          onClick={() => onPageChange(page - 1)}
          disabled={page <= 1}
        >
          Previous
        </button>
        <button
          type="button"
          className="h-8 rounded-control border border-line bg-surface px-3 text-secondary disabled:opacity-40"
          onClick={() => onPageChange(page + 1)}
          disabled={page >= pageCount}
        >
          Next
        </button>
      </div>
    </nav>
  )
}

export function FilterBar({ children }: { children: ReactNode }) {
  return <div className="flex flex-col gap-3 md:flex-row md:items-center">{children}</div>
}

const tones = {
  success: 'bg-green-soft text-green-ink',
  warning: 'bg-warning-soft text-warning-ink',
  danger: 'bg-danger-soft text-danger-ink',
  info: 'bg-info-soft text-info-ink',
  neutral: 'bg-surface-muted text-ink-secondary',
} as const

export function StatusBadge({
  tone = 'neutral',
  children,
}: {
  tone?: keyof typeof tones
  children: ReactNode
}) {
  return (
    <span className={cn('inline-flex items-center rounded-full px-2 py-0.5 text-meta font-medium', tones[tone])}>
      {children}
    </span>
  )
}

export function SkeletonTable() {
  return (
    <div className="space-y-2 rounded-card border border-line bg-surface p-4" aria-hidden="true">
      {Array.from({ length: 5 }).map((_, index) => (
        <div key={index} className="h-8 animate-pulse rounded-control bg-surface-muted" />
      ))}
    </div>
  )
}
