import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Button } from '@/components/ui/Button'
import { FormField, Select, TextInput } from '@/components/forms/fields'
import { PageContainer, PageHeader } from '@/components/layout/PageHeader'
import { DataTable, StatusBadge } from '@/components/tables/DataTable'
import { QueryPanel } from '@/features/workspace/QueryPanel'
import { PhotoGallery } from '@/features/workspace/PhotoGallery'
import { toneFor } from '@/features/workspace/status'
import { api } from '@/lib/api'
import { titleCase } from '@/utils/format'

type Customer = { id: string; displayName: string }
type Project = { id: string; customerId: string; name: string; status: string; capacityKw: string | null; targetDate: string | null; progressPercent: number }
type Task = { id: string; title: string; description: string | null; status: string; dueOn: string | null }
type Photo = { id: string; fileName: string; caption: string | null }
type Detail = { project: Project; tasks: Task[]; photos: Photo[] }

export function ProjectsPage() {
  const client = useQueryClient()
  const [customerId, setCustomerId] = useState('')
  const [name, setName] = useState('')
  const customers = useQuery({
    queryKey: ['customers', 'options'],
    queryFn: async () => (await api.get<{ items: Customer[] }>('/v1/customers?size=100')).data.items,
  })
  const projects = useQuery({ queryKey: ['projects'], queryFn: async () => (await api.get<Project[]>('/v1/projects')).data })
  const create = useMutation({
    mutationFn: async () =>
      api.post('/v1/projects', { customerId, name, status: 'PLANNED', progressPercent: 0 }),
    onSuccess: async () => {
      setName('')
      await client.invalidateQueries({ queryKey: ['projects'] })
    },
  })

  return (
    <PageContainer>
      <PageHeader title="Projects" description="Installation progress from the accepted quotation through commissioning." />
      <form
        className="grid gap-4 rounded-card border border-line bg-surface p-5 md:grid-cols-3"
        onSubmit={(event) => {
          event.preventDefault()
          create.mutate()
        }}
      >
        <FormField label="Customer" htmlFor="project-customer">
          <Select id="project-customer" value={customerId} onChange={(event) => setCustomerId(event.target.value)} required>
            <option value="">Select a customer</option>
            {(customers.data ?? []).map((customer) => (
              <option key={customer.id} value={customer.id}>
                {customer.displayName}
              </option>
            ))}
          </Select>
        </FormField>
        <FormField label="Project" htmlFor="project-name">
          <TextInput id="project-name" value={name} onChange={(event) => setName(event.target.value)} required />
        </FormField>
        <div className="flex items-end">
          <Button type="submit" disabled={!customerId || name.trim().length === 0}>
            Create project
          </Button>
        </div>
      </form>
      <QueryPanel
        isLoading={projects.isLoading}
        isError={projects.isError}
        onRetry={() => void projects.refetch()}
        isEmpty={(projects.data?.length ?? 0) === 0}
        empty={{ title: 'No installations', description: 'Accept a quotation or create a project for a customer.' }}
      >
        <DataTable
          rows={projects.data ?? []}
          getRowId={(row) => row.id}
          empty={null}
          columns={[
            { id: 'name', header: 'Project', cell: (row) => <Link to={`/projects/${row.id}`}>{row.name}</Link> },
            { id: 'kw', header: 'kW', cell: (row) => row.capacityKw ?? '—' },
            { id: 'target', header: 'Target', cell: (row) => row.targetDate ?? '—' },
            { id: 'progress', header: 'Progress', cell: (row) => `${row.progressPercent}%` },
            { id: 'status', header: 'Status', cell: (row) => <StatusBadge tone={toneFor(row.status)}>{titleCase(row.status)}</StatusBadge> },
          ]}
        />
      </QueryPanel>
    </PageContainer>
  )
}

export function ProjectDetailPage() {
  const { projectId = '' } = useParams()
  const client = useQueryClient()
  const [title, setTitle] = useState('')
  const detail = useQuery({
    queryKey: ['project', projectId],
    queryFn: async () => (await api.get<Detail>(`/v1/projects/${projectId}`)).data,
  })
  const addTask = useMutation({
    mutationFn: async () => api.post(`/v1/projects/${projectId}/tasks`, { title, status: 'TODO', sortOrder: 1 }),
    onSuccess: async () => {
      setTitle('')
      await client.invalidateQueries({ queryKey: ['project', projectId] })
    },
  })
  const upload = useMutation({
    mutationFn: async (file: File) => {
      const body = new FormData()
      body.append('file', file)
      await api.post(`/v1/projects/${projectId}/photos`, body)
    },
    onSuccess: async () => client.invalidateQueries({ queryKey: ['project', projectId] }),
  })
  const updateTask = useMutation({
    mutationFn: async (task: Task) =>
      api.patch(`/v1/projects/${projectId}/tasks/${task.id}`, {
        title: task.title,
        description: task.description,
        status: task.status,
        dueOn: task.dueOn,
      }),
    onSuccess: async () => client.invalidateQueries({ queryKey: ['project', projectId] }),
  })

  return (
    <PageContainer>
      <QueryPanel isLoading={detail.isLoading} isError={detail.isError} onRetry={() => void detail.refetch()}>
        {detail.data ? (
          <>
            <PageHeader
              title={detail.data.project.name}
              description={`Target ${detail.data.project.targetDate ?? 'not set'} · ${detail.data.project.progressPercent}% complete`}
            />
            <form
              className="flex flex-col gap-3 rounded-card border border-line bg-surface p-5 sm:flex-row"
              onSubmit={(event) => {
                event.preventDefault()
                addTask.mutate()
              }}
            >
              <TextInput aria-label="Task title" value={title} onChange={(event) => setTitle(event.target.value)} placeholder="Next site task" />
              <Button type="submit" disabled={title.trim().length === 0}>
                Add task
              </Button>
            </form>
            <DataTable
              rows={detail.data.tasks}
              getRowId={(row) => row.id}
              empty={<p className="text-secondary text-ink-secondary">No tasks on this installation.</p>}
              columns={[
                { id: 'title', header: 'Task', cell: (row) => row.title },
                { id: 'due', header: 'Due', cell: (row) => row.dueOn ?? '—' },
                {
                  id: 'status',
                  header: 'Status',
                  cell: (row) => (
                    <select
                      aria-label={`Status for ${row.title}`}
                      className="h-8 rounded-control border border-line bg-surface px-2 text-secondary"
                      value={row.status}
                      onChange={(event) => updateTask.mutate({ ...row, status: event.target.value })}
                    >
                      {['TODO', 'IN_PROGRESS', 'DONE'].map((status) => (
                        <option key={status} value={status}>
                          {titleCase(status)}
                        </option>
                      ))}
                    </select>
                  ),
                },
              ]}
            />
            <section className="flex flex-col gap-3">
              <h2 className="text-card text-ink">Site photos</h2>
              <input
                aria-label="Upload project photo"
                type="file"
                accept="image/png,image/jpeg"
                onChange={(event) => {
                  const file = event.target.files?.[0]
                  if (file) upload.mutate(file)
                }}
              />
              <PhotoGallery
                photos={(detail.data.photos ?? []).map((photo) => ({
                  id: photo.id,
                  name: photo.caption || photo.fileName,
                  path: `/v1/projects/${projectId}/photos/${photo.id}`,
                }))}
              />
            </section>
          </>
        ) : null}
      </QueryPanel>
    </PageContainer>
  )
}
