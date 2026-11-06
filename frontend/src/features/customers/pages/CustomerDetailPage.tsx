import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { PageContainer, PageHeader } from '@/components/layout/PageHeader'
import { DataTable, StatusBadge } from '@/components/tables/DataTable'
import { QueryPanel } from '@/features/workspace/QueryPanel'
import { toneFor } from '@/features/workspace/status'
import { api } from '@/lib/api'
import { inr, titleCase } from '@/utils/format'

type Customer = { id: string; displayName: string; phone: string | null; email: string | null; city: string | null; state: string | null; status: string; notes: string | null }
type Project = { id: string; customerId: string; name: string; status: string; capacityKw: string | null; progressPercent: number }
type Payment = { id: string; customerId: string; amount: string; status: string; method: string }

export function CustomerDetailPage() {
  const { customerId = '' } = useParams()
  const customer = useQuery({
    queryKey: ['customer', customerId],
    queryFn: async () => (await api.get<Customer>(`/v1/customers/${customerId}`)).data,
  })
  const projects = useQuery({
    queryKey: ['projects'],
    queryFn: async () => (await api.get<Project[]>('/v1/projects')).data,
  })
  const payments = useQuery({
    queryKey: ['payments'],
    queryFn: async () => (await api.get<{ items: Payment[] }>('/v1/payments')).data.items,
  })
  const ownedProjects = (projects.data ?? []).filter((item) => item.customerId === customerId)
  const ownedPayments = (payments.data ?? []).filter((item) => item.customerId === customerId)

  return (
    <PageContainer>
      <QueryPanel isLoading={customer.isLoading} isError={customer.isError} onRetry={() => void customer.refetch()}>
        {customer.data ? (
          <>
            <PageHeader
              title={customer.data.displayName}
              description={[customer.data.phone, customer.data.email, customer.data.city, customer.data.state].filter(Boolean).join(' · ')}
              actions={<StatusBadge tone={toneFor(customer.data.status)}>{titleCase(customer.data.status)}</StatusBadge>}
            />
            <section className="grid gap-4 lg:grid-cols-2">
              <div className="flex flex-col gap-3">
                <h2 className="text-card text-ink">Projects</h2>
                <DataTable
                  rows={ownedProjects}
                  getRowId={(row) => row.id}
                  empty={<p className="text-secondary text-ink-secondary">No installations for this customer.</p>}
                  columns={[
                    { id: 'name', header: 'Project', cell: (row) => <Link to={`/projects/${row.id}`}>{row.name}</Link> },
                    { id: 'kw', header: 'kW', cell: (row) => row.capacityKw ?? '—' },
                    { id: 'progress', header: 'Progress', cell: (row) => `${row.progressPercent}%` },
                  ]}
                />
              </div>
              <div className="flex flex-col gap-3">
                <h2 className="text-card text-ink">Payments</h2>
                <DataTable
                  rows={ownedPayments}
                  getRowId={(row) => row.id}
                  empty={<p className="text-secondary text-ink-secondary">No payments recorded.</p>}
                  columns={[
                    { id: 'amount', header: 'Amount', cell: (row) => inr(row.amount) },
                    { id: 'method', header: 'Method', cell: (row) => titleCase(row.method) },
                    { id: 'status', header: 'Status', cell: (row) => <StatusBadge tone={toneFor(row.status)}>{titleCase(row.status)}</StatusBadge> },
                  ]}
                />
              </div>
            </section>
          </>
        ) : null}
      </QueryPanel>
    </PageContainer>
  )
}
