import { useEffect, useRef, type ReactNode } from 'react'
import { Button } from '@/components/ui/Button'

export function ConfirmDialog({
  open,
  title,
  description,
  confirmLabel = 'Confirm',
  tone = 'default',
  onConfirm,
  onClose,
}: {
  open: boolean
  title: string
  description: string
  confirmLabel?: string
  tone?: 'default' | 'danger'
  onConfirm: () => void
  onClose: () => void
}) {
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
      className="w-[min(100%-2rem,28rem)] rounded-card bg-transparent"
      onClose={onClose}
      aria-labelledby="confirm-title"
    >
      <div className="rounded-card border border-line bg-surface p-5 shadow-pop">
        <h2 id="confirm-title" className="text-title text-ink">
          {title}
        </h2>
        <p className="mt-2 text-body text-ink-secondary">{description}</p>
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button variant={tone === 'danger' ? 'danger' : 'primary'} onClick={onConfirm}>
            {confirmLabel}
          </Button>
        </div>
      </div>
    </dialog>
  )
}

export function Alert({
  tone = 'info',
  title,
  children,
}: {
  tone?: 'info' | 'warning' | 'danger' | 'success'
  title: string
  children?: ReactNode
}) {
  const styles = {
    info: 'border-line bg-info-soft',
    warning: 'border-line bg-warning-soft',
    danger: 'border-line bg-danger-soft',
    success: 'border-line bg-green-soft',
  }[tone]
  return (
    <div role="status" className={`rounded-card border px-4 py-3 ${styles}`}>
      <p className="text-body font-medium text-ink">{title}</p>
      {children ? <div className="mt-1 text-secondary text-ink-secondary">{children}</div> : null}
    </div>
  )
}

export function ErrorState({
  title = 'Something went wrong',
  description,
  onRetry,
}: {
  title?: string
  description: string
  onRetry?: () => void
}) {
  return (
    <div className="rounded-card border border-line bg-surface px-6 py-10 text-center shadow-card">
      <h2 className="text-title text-ink">{title}</h2>
      <p className="mx-auto mt-2 max-w-md text-body text-ink-secondary">{description}</p>
      {onRetry ? (
        <div className="mt-4">
          <Button variant="secondary" onClick={onRetry}>
            Try again
          </Button>
        </div>
      ) : null}
    </div>
  )
}

export function LoadingSpinner({ label = 'Loading' }: { label?: string }) {
  return (
    <div role="status" className="inline-flex items-center gap-2 text-secondary text-ink-secondary">
      <span className="h-4 w-4 animate-spin rounded-full border-2 border-line border-t-navy" />
      {label}
    </div>
  )
}

export function SkeletonLoader({ className = 'h-24' }: { className?: string }) {
  return <div className={`animate-pulse rounded-card bg-surface-muted ${className}`} aria-hidden="true" />
}

export function EmptyState({
  title,
  description,
  action,
}: {
  title: string
  description: string
  action?: ReactNode
}) {
  return (
    <div className="rounded-card border border-dashed border-line bg-surface px-6 py-12 text-center">
      <h2 className="text-title text-ink">{title}</h2>
      <p className="mx-auto mt-2 max-w-lg text-body text-ink-secondary">{description}</p>
      {action ? <div className="mt-4">{action}</div> : null}
    </div>
  )
}
