import { api } from '@/lib/api'
import type { HealthResponse } from '@/types/health'

export async function fetchHealth() {
  const response = await api.get<HealthResponse>('/v1/health')
  return response.data
}
