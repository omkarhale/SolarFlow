import { useState, type ReactNode } from 'react'
import { SessionContext } from '@/features/auth/session-context'
import type { UserProfile } from '@/lib/api'

export function SessionProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfile | null>(null)
  return <SessionContext.Provider value={{ user, setUser }}>{children}</SessionContext.Provider>
}
