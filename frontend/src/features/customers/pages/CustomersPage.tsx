import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/Button'
import { FormField, Select, TextInput } from '@/components/forms/fields'
import { PageContainer, PageHeader } from '@/components/layout/PageHeader'
import { DataTable, FilterBar, Pagination, StatusBadge } from '@/components/tables/DataTable'
import { QueryPanel } from '@/features/workspace/QueryPanel'
import { toneFor } from '@/features/workspace/status'
import { api } from '@/lib/api'
import { titleCase } from '@/utils/format'

type Customer = {
  id: string
  displayName: string
  phone: string | null
  email: string | null
  city: string | null
  status: string
  source: string | null
}

type Page<T> = { items: T[]; page: number; size: number; total: number }

export function CustomersPage() {
  const client = useQueryClient()
  const [q, setQ] = useState('')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)
  const [open, setOpen] = useState(false)
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [city, setCity] = useState('')
  const customers = useQuery({
    queryKey: ['customers', q, status, page],
    queryFn: async () =>
      (
        await api.get<Page<Customer>>('/v1/customers', {
          params: { q: q || undefined, status: status || undefined, page, size: 20 },
        })
      ).data,
  })
  const create = useMutation({
    mutationFn: async () =>
      api.post('/v1/customers', { displayName: name, phone, city, state: 'Maharashtra', status: 'ACTIVE', source: 'Walk-in' }),
    onSuccess: async () => {
      setName('')
      setPhone('')
      setCity('')
      setOpen(false)
      await client.invalidateQueries({ queryKey: ['customers'] })
    },
  })
  const total = customers.data?.total ?? 0
  const pageCount = Math.max(1, Math.ceil(total / 20))

  return (
    <PageContainer>
      <PageHeader
        title="Customers"
        description="Households and businesses this company has quoted or installed."
        actions={
          <Button onClick={() => setOpen((value) => !value)}>{open ? 'Close form' : 'Add customer'}</Button>
        }
      />
      {open ? (
        <form
          className="grid gap-4 rounded-card border border-line bg-surface p-5 md:grid-cols-3"
          onSubmit={(event) => {
            event.preventDefault()
            create.mutate()
          }}
        >
          <FormField label="Name" htmlFor="customer-name">
            <TextInput id="customer-name" value={name} onChange={(event) => setName(event.target.value)} required />
          </FormField>
          <FormField label="Phone" htmlFor="customer-phone">
            <TextInput id="customer-phone" value={phone} onChange={(event) => setPhone(event.target.value)} />
          </FormField>
          <FormField label="City" htmlFor="customer-city">
            <TextInput id="customer-city" value={city} onChange={(event) => setCity(event.target.value)} />
          </FormField>
          <Button type="submit" disabled={create.isPending || name.trim().length === 0}>
            Save customer
          </Button>
        </form>
      ) : null}
      <FilterBar>
        <TextInput
          aria-label="Search customers"
          placeholder="Search name, phone, or city"
          value={q}
          onChange={(event) => {
            setPage(0)
            setQ(event.target.value)
          }}
        />
        <Select
          aria-label="Customer status"
          value={status}
          onChange={(event) => {
            setPage(0)
            setStatus(event.target.value)
          }}
        >
          <option value="">All statuses</option>
          <option value="ACTIVE">Active</option>
          <option value="INACTIVE">Inactive</option>
        </Select>
      </FilterBar>
      <QueryPanel
        isLoading={customers.isLoading}
        isError={customers.isError}
        onRetry={() => void customers.refetch()}
        isEmpty={(customers.data?.items.length ?? 0) === 0}
        empty={{ title: 'No customers match', description: 'Add a household or clear the search.' }}
      >
        <DataTable
          rows={customers.data?.items ?? []}
          getRowId={(row) => row.id}
          empty={null}
          columns={[
            {
              id: 'name',
              header: 'Customer',
              cell: (row) => (
                <Link className="font-medium text-ink underline-offset-2 hover:underline" to={`/customers/${row.id}`}>
                  {row.displayName}
                </Link>
              ),
            },
            { id: 'phone', header: 'Phone', cell: (row) => row.phone || '—' },
            { id: 'city', header: 'City', cell: (row) => row.city || '—' },
            { id: 'source', header: 'Source', cell: (row) => row.source || '—' },
            {
              id: 'status',
              header: 'Status',
              cell: (row) => <StatusBadge tone={toneFor(row.status)}>{titleCase(row.status)}</StatusBadge>,
            },
          ]}
        />
        <Pagination page={page + 1} pageCount={pageCount} onPageChange={(next) => setPage(next - 1)} />
      </QueryPanel>
    </PageContainer>
  )
}
