import { useEffect, useState } from 'react'
import { api } from '@/lib/api'

export function PhotoGallery({ photos }: { photos: { id: string; path: string; name: string }[] }) {
  if (photos.length === 0) {
    return <p className="text-secondary text-ink-secondary">No photos yet.</p>
  }
  return (
    <ul className="flex flex-wrap gap-3">
      {photos.map((photo) => (
        <li key={photo.id}>
          <AuthImage path={photo.path} name={photo.name} />
        </li>
      ))}
    </ul>
  )
}

function AuthImage({ path, name }: { path: string; name: string }) {
  const [src, setSrc] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    let objectUrl = ''
    api
      .get(path, { responseType: 'blob' })
      .then((response) => {
        objectUrl = URL.createObjectURL(response.data)
        if (active) setSrc(objectUrl)
      })
      .catch(() => {
        if (active) setSrc(null)
      })
    return () => {
      active = false
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [path])

  if (!src) {
    return <div className="h-24 w-24 animate-pulse rounded-control bg-surface-muted" aria-label={name} />
  }
  return <img src={src} alt={name} className="h-24 w-24 rounded-control border border-line object-cover" />
}
