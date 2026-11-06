import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { getAccessToken, readCsrfToken, setAccessToken } from '@/lib/session'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  withCredentials: true,
})

api.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  const csrf = readCsrfToken()
  if (csrf) config.headers['X-XSRF-TOKEN'] = csrf
  return config
})

let refreshTask: Promise<string | null> | null = null

export async function refreshSession() {
  const response = await api.post<AuthPayload>('/v1/auth/refresh')
  setAccessToken(response.data.accessToken)
  return response.data
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { retried?: boolean }) | undefined
    const url = original?.url ?? ''
    if (
      error.response?.status === 401 &&
      original &&
      !original.retried &&
      !url.includes('/auth/login') &&
      !url.includes('/auth/refresh')
    ) {
      original.retried = true
      try {
        refreshTask ??= refreshSession()
          .then((payload) => payload.accessToken)
          .finally(() => {
            refreshTask = null
          })
        const token = await refreshTask
        if (!token) return Promise.reject(error)
        original.headers.Authorization = `Bearer ${token}`
        return api(original)
      } catch (refreshError) {
        setAccessToken(null)
        return Promise.reject(refreshError)
      }
    }
    return Promise.reject(error)
  },
)

export type UserProfile = {
  id: string
  companyId: string
  email: string
  firstName: string
  lastName: string
  phone?: string
  role: string
  status: string
}

export type AuthPayload = {
  accessToken: string
  expiresIn: number
  user: UserProfile
}
