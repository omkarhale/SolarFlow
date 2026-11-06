import type { ReactNode } from 'react'
import { EmptyState, ErrorState } from '@/components/feedback/states'
import { SkeletonTable } from '@/components/tables/DataTable'

export function QueryPanel({
  isLoading,
  isError,
  onRetry,
  empty,
  isEmpty,
  children,
}: {
  isLoading: boolean
  isError: boolean
  onRetry: () => void
  empty?: { title: string; description: string }
  isEmpty?: boolean
  children: ReactNode
}) {
  if (isLoading) return <SkeletonTable />
  if (isError) {
    return <ErrorState description="The workspace could not load this list." onRetry={onRetry} />
  }
  if (isEmpty && empty) return <EmptyState title={empty.title} description={empty.description} />
  return <>{children}</>
}
