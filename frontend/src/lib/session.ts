let accessToken: string | null = null

export function setAccessToken(token: string | null) {
  accessToken = token
}

export function getAccessToken() {
  return accessToken
}

export function readCsrfToken() {
  const row = document.cookie.split('; ').find((part) => part.startsWith('XSRF-TOKEN='))
  if (!row) return null
  return decodeURIComponent(row.slice('XSRF-TOKEN='.length))
}
