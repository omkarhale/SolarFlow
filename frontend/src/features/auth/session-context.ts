import { createContext, useContext } from 'react'
import type { UserProfile } from '@/lib/api'

export type SessionValue = {
  user: UserProfile | null
  setUser: (user: UserProfile | null) => void
}

export const SessionContext = createContext<SessionValue | null>(null)

export function useSession() {
  const value = useContext(SessionContext)
  if (!value) throw new Error('useSession must be used within SessionProvider')
  return value
}
