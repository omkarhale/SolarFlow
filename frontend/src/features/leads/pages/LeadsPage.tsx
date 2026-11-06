import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Button } from '@/components/ui/Button'
import { FormField, TextInput } from '@/components/forms/fields'
import { PageContainer, PageHeader } from '@/components/layout/PageHeader'
import { StatusBadge } from '@/components/tables/DataTable'
import { QueryPanel } from '@/features/workspace/QueryPanel'
import { toneFor } from '@/features/workspace/status'
import { api } from '@/lib/api'
import { titleCase } from '@/utils/format'

type Lead = {
  id: string
  contactName: string
  phone: string | null
  city: string | null
  status: string
  priority: string
  followUpOn: string | null
  estimatedCapacityKw: string | null
}

const columns = ['NEW', 'CONTACTED', 'SURVEY_SCHEDULED', 'QUOTED', 'NEGOTIATION', 'WON', 'LOST']

export function LeadsPage() {
  const client = useQueryClient()
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const leads = useQuery({
    queryKey: ['leads'],
    queryFn: async () => (await api.get<Lead[]>('/v1/leads')).data,
  })
  const create = useMutation({
    mutationFn: async () =>
      api.post('/v1/leads', {
        contactName: name,
        phone,
        status: 'NEW',
        priority: 'MEDIUM',
        source: 'Website',
      }),
    onSuccess: async () => {
      setName('')
      setPhone('')
      await client.invalidateQueries({ queryKey: ['leads'] })
    },
  })
  const move = useMutation({
    mutationFn: async ({ lead, status }: { lead: Lead; status: string }) =>
      api.patch(`/v1/leads/${lead.id}`, { ...lead, status }),
    onSuccess: async () => client.invalidateQueries({ queryKey: ['leads'] }),
  })
  const convert = useMutation({
    mutationFn: async (id: string) => api.post(`/v1/leads/${id}/convert`),
    onSuccess: async () => {
      await client.invalidateQueries({ queryKey: ['leads'] })
      await client.invalidateQueries({ queryKey: ['customers'] })
    },
  })

  return (
    <PageContainer>
      <PageHeader title="Leads" description="Move an enquiry from first contact through a signed project." />
      <form
        className="grid gap-4 rounded-card border border-line bg-surface p-5 md:grid-cols-[1fr_1fr_auto]"
        onSubmit={(event) => {
          event.preventDefault()
          create.mutate()
        }}
      >
        <FormField label="Contact" htmlFor="lead-name">
          <TextInput id="lead-name" value={name} onChange={(event) => setName(event.target.value)} required />
        </FormField>
        <FormField label="Phone" htmlFor="lead-phone">
          <TextInput id="lead-phone" value={phone} onChange={(event) => setPhone(event.target.value)} />
        </FormField>
        <div className="flex items-end">
          <Button type="submit" disabled={name.trim().length === 0 || create.isPending}>
            Add lead
          </Button>
        </div>
      </form>
      <QueryPanel
        isLoading={leads.isLoading}
        isError={leads.isError}
        onRetry={() => void leads.refetch()}
        isEmpty={(leads.data?.length ?? 0) === 0}
        empty={{ title: 'No enquiries yet', description: 'Add the first household that asked about rooftop solar.' }}
      >
        <div className="grid gap-3 overflow-x-auto xl:grid-cols-7">
          {columns.map((status) => (
            <section key={status} className="min-w-48 rounded-card border border-line bg-surface-muted p-3">
              <h2 className="mb-2 text-label text-ink-secondary">{titleCase(status)}</h2>
              <div className="flex flex-col gap-2">
                {(leads.data ?? [])
                  .filter((lead) => lead.status === status)
                  .map((lead) => (
                    <article key={lead.id} className="rounded-control border border-line bg-surface p-3">
                      <p className="text-body text-ink">{lead.contactName}</p>
                      <p className="text-meta text-ink-secondary">{lead.city || lead.phone || 'No location'}</p>
                      <div className="mt-2">
                        <StatusBadge tone={toneFor(lead.priority)}>{titleCase(lead.priority)}</StatusBadge>
                      </div>
                      <label className="mt-2 block text-meta text-ink-secondary" htmlFor={`move-${lead.id}`}>
                        Stage
                        <select
                          id={`move-${lead.id}`}
                          className="mt-1 h-8 w-full rounded-control border border-line bg-surface px-2 text-secondary"
                          value={lead.status}
                          onChange={(event) => move.mutate({ lead, status: event.target.value })}
                        >
                          {columns.map((option) => (
                            <option key={option} value={option}>
                              {titleCase(option)}
                            </option>
                          ))}
                        </select>
                      </label>
                      {lead.status !== 'WON' && lead.status !== 'LOST' ? (
                        <Button className="mt-2 w-full" size="sm" variant="secondary" onClick={() => convert.mutate(lead.id)}>
                          Convert
                        </Button>
                      ) : null}
                    </article>
                  ))}
              </div>
            </section>
          ))}
        </div>
      </QueryPanel>
    </PageContainer>
  )
}
