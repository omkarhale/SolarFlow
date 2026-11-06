import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Button } from '@/components/ui/Button'
import { FormField, Select, TextInput } from '@/components/forms/fields'
import { PageContainer, PageHeader } from '@/components/layout/PageHeader'
import { DataTable, StatusBadge } from '@/components/tables/DataTable'
import { QueryPanel } from '@/features/workspace/QueryPanel'
import { toneFor } from '@/features/workspace/status'
import { api } from '@/lib/api'
import { titleCase } from '@/utils/format'

type Customer = { id: string; displayName: string }
type Warranty = { id: string; customerId: string; warrantyType: string; endDate: string; status: string }
type Request = { id: string; customerId: string; title: string; priority: string; status: string }

export function ServicePage() {
  const client = useQueryClient()
  const [customerId, setCustomerId] = useState('')
  const [title, setTitle] = useState('')
  const customers = useQuery({
    queryKey: ['customers', 'options'],
    queryFn: async () => (await api.get<{ items: Customer[] }>('/v1/customers?size=100')).data.items,
  })
  const warranties = useQuery({
    queryKey: ['warranties'],
    queryFn: async () => (await api.get<{ items: Warranty[]; expiring: Warranty[] }>('/v1/warranties')).data,
  })
  const requests = useQuery({
    queryKey: ['service-requests'],
    queryFn: async () => (await api.get<Request[]>('/v1/service-requests')).data,
  })
  const names = new Map((customers.data ?? []).map((customer) => [customer.id, customer.displayName]))
  const create = useMutation({
    mutationFn: async () => api.post('/v1/service-requests', { customerId, title, priority: 'MEDIUM', status: 'OPEN' }),
    onSuccess: async () => {
      setTitle('')
      await client.invalidateQueries({ queryKey: ['service-requests'] })
    },
  })

  return (
    <PageContainer>
      <PageHeader title="Service" description="Warranties approaching their end date and open service requests." />
      <section className="grid gap-4 lg:grid-cols-2">
        <div className="flex flex-col gap-3">
          <h2 className="text-card text-ink">Expiring within 60 days</h2>
          <QueryPanel
            isLoading={warranties.isLoading}
            isError={warranties.isError}
            onRetry={() => void warranties.refetch()}
            isEmpty={(warranties.data?.expiring.length ?? 0) === 0}
            empty={{ title: 'No warranties expiring', description: 'Covered systems stay off this list until the end date is close.' }}
          >
            <DataTable
              rows={warranties.data?.expiring ?? []}
              getRowId={(row) => row.id}
              empty={null}
              columns={[
                { id: 'customer', header: 'Customer', cell: (row) => names.get(row.customerId) ?? 'Customer' },
                { id: 'type', header: 'Cover', cell: (row) => titleCase(row.warrantyType) },
                { id: 'end', header: 'Ends', cell: (row) => row.endDate },
                { id: 'status', header: 'Status', cell: (row) => <StatusBadge tone={toneFor(row.status)}>{titleCase(row.status)}</StatusBadge> },
              ]}
            />
          </QueryPanel>
        </div>
        <div className="flex flex-col gap-3">
          <h2 className="text-card text-ink">Requests</h2>
          <form
            className="grid gap-3 rounded-card border border-line bg-surface p-4"
            onSubmit={(event) => {
              event.preventDefault()
              create.mutate()
            }}
          >
            <FormField label="Customer" htmlFor="service-customer">
              <Select id="service-customer" value={customerId} onChange={(event) => setCustomerId(event.target.value)} required>
                <option value="">Select a customer</option>
                {(customers.data ?? []).map((customer) => (
                  <option key={customer.id} value={customer.id}>
                    {customer.displayName}
                  </option>
                ))}
              </Select>
            </FormField>
            <FormField label="Issue" htmlFor="service-title">
              <TextInput id="service-title" value={title} onChange={(event) => setTitle(event.target.value)} required />
            </FormField>
            <Button type="submit" disabled={!customerId || title.trim().length === 0}>
              Open request
            </Button>
          </form>
          <QueryPanel
            isLoading={requests.isLoading}
            isError={requests.isError}
            onRetry={() => void requests.refetch()}
            isEmpty={(requests.data?.length ?? 0) === 0}
            empty={{ title: 'No service requests', description: 'Open a ticket when a commissioned system needs a visit.' }}
          >
            <DataTable
              rows={requests.data ?? []}
              getRowId={(row) => row.id}
              empty={null}
              columns={[
                { id: 'title', header: 'Request', cell: (row) => row.title },
                { id: 'customer', header: 'Customer', cell: (row) => names.get(row.customerId) ?? 'Customer' },
                { id: 'priority', header: 'Priority', cell: (row) => <StatusBadge tone={toneFor(row.priority)}>{titleCase(row.priority)}</StatusBadge> },
                { id: 'status', header: 'Status', cell: (row) => <StatusBadge tone={toneFor(row.status)}>{titleCase(row.status)}</StatusBadge> },
              ]}
            />
          </QueryPanel>
        </div>
      </section>
    </PageContainer>
  )
}
