import { forwardRef, type ButtonHTMLAttributes } from 'react'
import { cn } from '@/lib/cn'

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger'
  size?: 'md' | 'sm'
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(function Button(
  { className, variant = 'primary', size = 'md', type = 'button', ...props },
  ref,
) {
  return (
    <button
      ref={ref}
      type={type}
      className={cn(
        'inline-flex items-center justify-center gap-2 rounded-control font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-50',
        size === 'md' ? 'h-10 px-3.5 text-body' : 'h-8 px-2.5 text-secondary',
        variant === 'primary' && 'bg-navy text-[var(--sf-on-primary)] hover:bg-[var(--sf-primary-hover)]',
        variant === 'secondary' && 'border border-line bg-surface text-ink hover:bg-surface-muted',
        variant === 'ghost' && 'text-ink-secondary hover:bg-surface-muted hover:text-ink',
        variant === 'danger' && 'bg-danger text-white hover:brightness-95',
        className,
      )}
      {...props}
    />
  )
})
