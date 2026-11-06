import {
  forwardRef,
  type InputHTMLAttributes,
  type ReactNode,
  type SelectHTMLAttributes,
  type TextareaHTMLAttributes,
} from 'react'
import { cn } from '@/lib/cn'

const controlClass =
  'h-10 w-full rounded-control border border-line bg-surface px-3 text-body text-ink placeholder:text-ink-muted disabled:bg-surface-muted disabled:text-ink-muted'

type FieldProps = {
  label: string
  htmlFor: string
  hint?: string
  error?: string
  children: ReactNode
}

export function FormField({ label, htmlFor, hint, error, children }: FieldProps) {
  const hintId = hint ? `${htmlFor}-hint` : undefined
  const errorId = error ? `${htmlFor}-error` : undefined
  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={htmlFor} className="text-label text-ink">
        {label}
      </label>
      <div aria-describedby={[hintId, errorId].filter(Boolean).join(' ') || undefined}>{children}</div>
      {hint ? (
        <p id={hintId} className="text-meta text-ink-secondary">
          {hint}
        </p>
      ) : null}
      {error ? (
        <p id={errorId} className="text-meta text-danger">
          {error}
        </p>
      ) : null}
    </div>
  )
}

export function FormSection({
  title,
  description,
  children,
}: {
  title: string
  description?: string
  children: ReactNode
}) {
  return (
    <section className="rounded-card border border-line bg-surface p-5 shadow-card">
      <header className="mb-4">
        <h2 className="text-card text-ink">{title}</h2>
        {description ? <p className="mt-1 text-secondary text-ink-secondary">{description}</p> : null}
      </header>
      <div className="grid gap-4 md:grid-cols-2">{children}</div>
    </section>
  )
}

export const TextInput = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
  function TextInput({ className, ...props }, ref) {
    return <input ref={ref} className={cn(controlClass, className)} {...props} />
  },
)

export const SearchInput = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
  function SearchInput({ className, ...props }, ref) {
    return (
      <input
        ref={ref}
        type="search"
        className={cn(controlClass, 'h-9 bg-surface-muted', className)}
        {...props}
      />
    )
  },
)

export const Select = forwardRef<HTMLSelectElement, SelectHTMLAttributes<HTMLSelectElement>>(
  function Select({ className, children, ...props }, ref) {
    return (
      <select ref={ref} className={cn(controlClass, className)} {...props}>
        {children}
      </select>
    )
  },
)

export const DatePicker = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
  function DatePicker({ className, ...props }, ref) {
    return <input ref={ref} type="date" className={cn(controlClass, className)} {...props} />
  },
)

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaHTMLAttributes<HTMLTextAreaElement>>(
  function Textarea({ className, ...props }, ref) {
    return (
      <textarea
        ref={ref}
        className={cn(controlClass, 'h-auto min-h-28 py-2', className)}
        {...props}
      />
    )
  },
)

export function Checkbox({
  label,
  ...props
}: InputHTMLAttributes<HTMLInputElement> & { label: string }) {
  return (
    <label className="inline-flex min-h-10 items-center gap-2 text-body text-ink">
      <input
        type="checkbox"
        className="h-4 w-4 rounded border-line text-navy focus-visible:outline-none"
        {...props}
      />
      {label}
    </label>
  )
}

export function RadioGroup({
  name,
  label,
  options,
  value,
  onChange,
}: {
  name: string
  label: string
  value: string
  options: { value: string; label: string }[]
  onChange: (value: string) => void
}) {
  return (
    <fieldset className="flex flex-col gap-2">
      <legend className="text-label text-ink">{label}</legend>
      <div className="flex flex-wrap gap-3">
        {options.map((option) => (
          <label key={option.value} className="inline-flex min-h-10 items-center gap-2 text-body">
            <input
              type="radio"
              name={name}
              value={option.value}
              checked={value === option.value}
              onChange={() => onChange(option.value)}
            />
            {option.label}
          </label>
        ))}
      </div>
    </fieldset>
  )
}

export function FileUploader({
  label,
  onFiles,
}: {
  label: string
  onFiles: (files: FileList) => void
}) {
  return (
    <label className="flex min-h-28 cursor-pointer flex-col items-center justify-center rounded-card border border-dashed border-line bg-surface-muted px-4 py-6 text-center">
      <span className="text-body font-medium text-ink">{label}</span>
      <span className="mt-1 text-meta text-ink-secondary">PNG or JPG, up to 10 MB</span>
      <input
        type="file"
        accept="image/png,image/jpeg"
        className="sr-only"
        onChange={(event) => {
          if (event.target.files) onFiles(event.target.files)
        }}
      />
    </label>
  )
}
