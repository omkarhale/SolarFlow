export function toneFor(status: string) {
  if (['ACTIVE', 'WON', 'ACCEPTED', 'COMPLETED', 'DONE', 'RECEIVED', 'RESOLVED', 'CLOSED'].includes(status)) {
    return 'success' as const
  }
  if (['PENDING', 'EXPIRING', 'HIGH', 'URGENT', 'IN_PROGRESS', 'NEGOTIATION', 'SENT'].includes(status)) {
    return 'warning' as const
  }
  if (['LOST', 'FAILED', 'CANCELLED', 'EXPIRED', 'REJECTED', 'VOID'].includes(status)) return 'danger' as const
  if (['NEW', 'OPEN', 'DRAFT', 'SCHEDULED', 'PLANNED'].includes(status)) return 'info' as const
  return 'neutral' as const
}
