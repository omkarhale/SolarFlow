import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Bell, Menu, Search } from 'lucide-react'
import { navigation } from '@/constants/navigation'
import { Breadcrumbs, DropdownMenu, MenuItem } from '@/components/common/navigation'
import { ThemeMenu } from '@/features/settings/components/ThemeMenu'
import { useSession } from '@/features/auth/session-context'
import { api } from '@/lib/api'
import { setAccessToken } from '@/lib/session'

export function TopNavbar({
  onOpenMenu,
  currentLabel,
}: {
  onOpenMenu: () => void
  currentLabel: string
}) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { user, setUser } = useSession()
  const company = useQuery({
    queryKey: ['company'],
    queryFn: async () => (await api.get<{ name: string }>('/v1/company')).data,
  })
  const notes = useQuery({
    queryKey: ['notifications'],
    queryFn: async () =>
      (await api.get<{ id: string; title: string; body: string | null; readAt: string | null }[]>('/v1/notifications')).data,
  })
  const [query, setQuery] = useState('')
  const [searchOpen, setSearchOpen] = useState(false)
  const [notesOpen, setNotesOpen] = useState(false)
  const searchRef = useRef<HTMLInputElement>(null)

  const matches = useMemo(() => {
    const term = query.trim().toLowerCase()
    if (!term) return []
    return navigation.filter((item) => item.label.toLowerCase().includes(term)).slice(0, 6)
  }, [query])

  useEffect(() => {
    function onKey(event: KeyboardEvent) {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault()
        searchRef.current?.focus()
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [])

  return (
    <header className="sticky top-0 z-20 flex h-16 items-center gap-3 border-b border-line bg-canvas px-4 md:px-6">
      <button
        type="button"
        className="inline-flex h-10 w-10 items-center justify-center rounded-control border border-line bg-surface md:hidden"
        aria-label="Open navigation"
        onClick={onOpenMenu}
      >
        <Menu size={18} />
      </button>
      <div className="hidden min-w-0 md:block">
        <Breadcrumbs
          items={
            currentLabel === 'Dashboard'
              ? [{ label: 'Dashboard' }]
              : [
                  { label: 'Workspace', to: '/' },
                  { label: currentLabel },
                ]
          }
        />
      </div>
      <div className="relative ml-auto w-full max-w-sm">
        <Search size={16} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-ink-muted" />
        <input
          ref={searchRef}
          value={query}
          onChange={(event) => {
            setQuery(event.target.value)
            setSearchOpen(true)
          }}
          onFocus={() => setSearchOpen(true)}
          placeholder="Search workspace"
          aria-label="Search workspace"
          className="h-9 w-full rounded-control border border-line bg-surface pl-9 pr-14 text-body placeholder:text-ink-muted"
        />
        <kbd className="pointer-events-none absolute right-2 top-1/2 hidden -translate-y-1/2 rounded border border-line px-1.5 text-meta text-ink-muted sm:inline">
          Ctrl K
        </kbd>
        {searchOpen && matches.length > 0 ? (
          <ul className="absolute z-30 mt-1 w-full overflow-hidden rounded-card border border-line bg-surface shadow-pop">
            {matches.map((item) => (
              <li key={item.to}>
                <button
                  type="button"
                  className="flex h-10 w-full items-center px-3 text-left text-body hover:bg-surface-muted"
                  onClick={() => {
                    navigate(item.to)
                    setQuery('')
                    setSearchOpen(false)
                  }}
                >
                  {item.label}
                </button>
              </li>
            ))}
          </ul>
        ) : null}
      </div>
      <div className="relative">
        <button
          type="button"
          className="inline-flex h-10 w-10 items-center justify-center rounded-control text-ink-secondary hover:bg-surface"
          aria-label="Notifications"
          aria-expanded={notesOpen}
          onClick={() => setNotesOpen((open) => !open)}
        >
          <Bell size={18} />
        </button>
        {notesOpen ? (
          <div className="absolute right-0 mt-1 w-72 rounded-card border border-line bg-surface p-4 shadow-pop">
            <p className="text-card text-ink">Notifications</p>
            {(notes.data ?? []).length === 0 ? (
              <p className="mt-1 text-secondary text-ink-secondary">You're caught up. New alerts will show here.</p>
            ) : (
              <ul className="mt-2 flex flex-col gap-2">
                {(notes.data ?? []).map((note) => (
                  <li key={note.id}>
                    <button
                      type="button"
                      className="w-full rounded-control px-2 py-1 text-left hover:bg-surface-muted"
                      onClick={() => {
                        void api.post(`/v1/notifications/${note.id}/read`).then(() => notes.refetch())
                      }}
                    >
                      <p className="text-body text-ink">{note.title}</p>
                      {note.body ? <p className="text-meta text-ink-secondary">{note.body}</p> : null}
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        ) : null}
      </div>
      <ThemeMenu />
      <DropdownMenu label="Account">
        <div className="px-2 py-2">
          <p className="text-meta text-ink-muted">{user ? `${user.firstName} ${user.lastName}` : 'Account'}</p>
          <p className="text-body text-ink">{company.data?.name ?? 'Company'}</p>
        </div>
        <MenuItem
          onSelect={() => {
            navigate('/settings')
          }}
        >
          Settings
        </MenuItem>
        <MenuItem
          onSelect={() => {
            void api.post('/v1/auth/logout').finally(() => {
              setAccessToken(null)
              setUser(null)
              queryClient.clear()
              navigate('/login')
            })
          }}
        >
          Sign out
        </MenuItem>
      </DropdownMenu>
    </header>
  )
}
