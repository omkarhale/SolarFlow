import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { Alert } from '@/components/feedback/states'
import { FormField, TextInput } from '@/components/forms/fields'
import { Button } from '@/components/ui/Button'
import { useSession } from '@/features/auth/session-context'
import { api, type AuthPayload } from '@/lib/api'
import { setAccessToken } from '@/lib/session'

const schema = z.object({
  email: z.string().email('Enter a valid email'),
  password: z.string().min(1, 'Enter your password'),
})

type FormValues = z.infer<typeof schema>

export function LoginPage() {
  const navigate = useNavigate()
  const { setUser } = useSession()
  const [error, setError] = useState<string | null>(null)
  const form = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { email: '', password: '' } })

  async function onSubmit(values: FormValues) {
    setError(null)
    try {
      await api.get('/v1/health')
      const response = await api.post<AuthPayload>('/v1/auth/login', values)
      setAccessToken(response.data.accessToken)
      setUser(response.data.user)
      navigate('/')
    } catch {
      setError('Email or password is incorrect.')
    }
  }

  return (
    <main className="grid min-h-screen place-items-center bg-canvas px-4">
      <div className="w-full max-w-md rounded-card border border-line bg-surface p-6 shadow-card">
        <div className="mb-6 flex items-center gap-3">
          <span className="flex h-10 w-10 items-center justify-center rounded-control bg-navy text-[var(--sf-on-primary)] text-card">
            SF
          </span>
          <div>
            <p className="text-card text-ink">SolarFlow</p>
            <p className="text-meta text-ink-secondary">Sign in to your company workspace</p>
          </div>
        </div>
        <form className="flex flex-col gap-4" onSubmit={form.handleSubmit(onSubmit)} noValidate>
          {error ? <Alert tone="danger" title={error} /> : null}
          <FormField label="Email" htmlFor="email" error={form.formState.errors.email?.message}>
            <TextInput id="email" type="email" autoComplete="username" {...form.register('email')} />
          </FormField>
          <FormField label="Password" htmlFor="password" error={form.formState.errors.password?.message}>
            <TextInput id="password" type="password" autoComplete="current-password" {...form.register('password')} />
          </FormField>
          <Button type="submit" disabled={form.formState.isSubmitting}>
            {form.formState.isSubmitting ? 'Signing in' : 'Sign in'}
          </Button>
        </form>
        <p className="mt-4 text-meta text-ink-secondary">
          Local workspace: owner@helios.example / Solarflow#2026
        </p>
      </div>
    </main>
  )
}
