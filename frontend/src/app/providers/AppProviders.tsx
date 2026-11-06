import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useState, type ReactNode } from 'react'
import { RouterProvider } from 'react-router-dom'
import { router } from '@/app/router/routes'
import { ThemeProvider } from '@/components/common/ThemeProvider'
import { ToastProvider } from '@/components/feedback/Toast'
import { SessionProvider } from '@/features/auth/SessionProvider'

export function AppProviders({ children }: { children?: ReactNode }) {
  const [client] = useState(() => new QueryClient())
  return (
    <QueryClientProvider client={client}>
      <ThemeProvider>
        <ToastProvider>
          <SessionProvider>{children ?? <RouterProvider router={router} />}</SessionProvider>
        </ToastProvider>
      </ThemeProvider>
    </QueryClientProvider>
  )
}
