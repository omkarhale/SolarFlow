import { useEffect, useState } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { LoadingSpinner } from '@/components/feedback/states'
import { useSession } from '@/features/auth/session-context'
import { api, refreshSession, type UserProfile } from '@/lib/api'
import { getAccessToken } from '@/lib/session'

export function RequireAuth() {
  const { setUser } = useSession()
  const [state, setState] = useState<'loading' | 'in' | 'out'>('loading')

  useEffect(() => {
    let active = true
    const existing = getAccessToken()
    const ready = existing
      ? api.get<UserProfile>('/v1/auth/me').then((response) => ({ user: response.data }))
      : refreshSession()
    ready
      .then((payload) => {
        if (!active) return
        setUser(payload.user)
        setState('in')
      })
      .catch(() => {
        if (!active) return
        setUser(null)
        setState('out')
      })
    return () => {
      active = false
    }
  }, [setUser])

  if (state === 'loading') {
    return (
      <div className="grid min-h-screen place-items-center bg-canvas">
        <LoadingSpinner label="Opening workspace" />
      </div>
    )
  }
  if (state === 'out') return <Navigate to="/login" replace />
  return <Outlet />
}
