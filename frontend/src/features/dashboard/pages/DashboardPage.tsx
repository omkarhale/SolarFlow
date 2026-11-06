import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Bar, BarChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { ChartCard, StatCard } from '@/components/charts/ChartCard'
import { PageContainer } from '@/components/layout/PageHeader'
import { DataTable, StatusBadge } from '@/components/tables/DataTable'
import { QueryPanel } from '@/features/workspace/QueryPanel'
import { toneFor } from '@/features/workspace/status'
import { useSession } from '@/features/auth/session-context'
import { api } from '@/lib/api'
import { formatLongDate, greetingFor } from '@/utils/datetime'
import { inr, titleCase } from '@/utils/format'

type Dashboard = {
  kpis: {
    openLeads: number
    pendingQuotations: number
    activeProjects: number
    outstanding: string
    collectedThisMonth: string
    pendingPayments: string
  }
  revenueByMonth: { month: string; amount: string }[]
  leadConversion: { won: number; lost: number; open: number }
  installationProgress: { id: string; name: string; progress: number; status: string }[]
  recentLeads: { id: string; contactName: string; status: string; priority: string; followUpOn: string | null }[]
  pendingQuotations: { id: string; number: string; status: string; totalAmount: string }[]
  upcomingInstallations: { id: string; name: string; targetDate: string | null; status: string }[]
  pendingPayments: { id: string; customerId: string; amount: string; status: string }[]
}

export function DashboardPage() {
  const { user } = useSession()
  const dashboard = useQuery({
    queryKey: ['dashboard'],
    queryFn: async () => (await api.get<Dashboard>('/v1/dashboard')).data,
  })
  const data = dashboard.data
  const revenue = (data?.revenueByMonth ?? []).map((point) => ({ month: point.month, amount: Number(point.amount) }))
  const conversion = data
    ? [
        { name: 'Won', count: data.leadConversion.won },
        { name: 'Open', count: data.leadConversion.open },
        { name: 'Lost', count: data.leadConversion.lost },
      ]
    : []

  return (
    <PageContainer>
      <section className="flex flex-col gap-1">
        <p className="text-label text-ink-secondary">{formatLongDate(new Date())}</p>
        <h1 className="text-display text-ink">
          {greetingFor(new Date())}
          {user ? `, ${user.firstName}` : ''}.
        </h1>
        <p className="max-w-2xl text-body text-ink-secondary">
          Open work, collections, and installations for this company.
        </p>
      </section>
      <QueryPanel
        isLoading={dashboard.isLoading}
        isError={dashboard.isError}
        onRetry={() => void dashboard.refetch()}
      >
        {data ? (
          <>
            <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
              <StatCard label="Open leads" value={String(data.kpis.openLeads)} hint="Not yet won or lost" />
              <StatCard label="Pending quotations" value={String(data.kpis.pendingQuotations)} hint="Draft and sent" />
              <StatCard label="Active projects" value={String(data.kpis.activeProjects)} hint="Planned or in progress" />
              <StatCard label="Collected this month" value={inr(data.kpis.collectedThisMonth)} />
              <StatCard label="Outstanding" value={inr(data.kpis.outstanding)} hint="Accepted quotes minus receipts" />
              <StatCard label="Pending payments" value={inr(data.kpis.pendingPayments)} />
            </section>
            <section className="grid gap-4 lg:grid-cols-2">
              <ChartCard title="Revenue" description="Received payments by month." empty={revenue.every((point) => point.amount === 0)}>
                <div className="h-56">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={revenue}>
                      <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                      <YAxis tick={{ fontSize: 12 }} />
                      <Tooltip />
                      <Bar dataKey="amount" fill="var(--sf-accent)" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </ChartCard>
              <ChartCard title="Lead conversion" description="Won, still open, and lost." empty={conversion.every((point) => point.count === 0)}>
                <div className="h-56">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={conversion}>
                      <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                      <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                      <Tooltip />
                      <Bar dataKey="count" fill="var(--sf-primary)" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </ChartCard>
            </section>
            <section className="grid gap-4 lg:grid-cols-2">
              <div className="flex flex-col gap-3">
                <h2 className="text-card text-ink">Upcoming installations</h2>
                <DataTable
                  rows={data.upcomingInstallations}
                  getRowId={(row) => row.id}
                  empty={<p className="text-secondary text-ink-secondary">No installations are scheduled.</p>}
                  columns={[
                    { id: 'name', header: 'Project', cell: (row) => <Link to={`/projects/${row.id}`}>{row.name}</Link> },
                    { id: 'date', header: 'Target', cell: (row) => row.targetDate ?? '—' },
                    { id: 'status', header: 'Status', cell: (row) => <StatusBadge tone={toneFor(row.status)}>{titleCase(row.status)}</StatusBadge> },
                  ]}
                />
              </div>
              <div className="flex flex-col gap-3">
                <h2 className="text-card text-ink">Waiting on the team</h2>
                <DataTable
                  rows={data.pendingQuotations}
                  getRowId={(row) => row.id}
                  empty={<p className="text-secondary text-ink-secondary">No quotations are waiting.</p>}
                  columns={[
                    { id: 'number', header: 'Quotation', cell: (row) => row.number },
                    { id: 'total', header: 'Total', cell: (row) => inr(row.totalAmount) },
                    { id: 'status', header: 'Status', cell: (row) => <StatusBadge tone={toneFor(row.status)}>{titleCase(row.status)}</StatusBadge> },
                  ]}
                />
              </div>
            </section>
            <DataTable
              rows={data.recentLeads}
              getRowId={(row) => row.id}
              empty={<p className="text-secondary text-ink-secondary">No leads yet.</p>}
              columns={[
                { id: 'name', header: 'Recent lead', cell: (row) => row.contactName },
                { id: 'follow', header: 'Follow up', cell: (row) => row.followUpOn ?? '—' },
                { id: 'status', header: 'Status', cell: (row) => <StatusBadge tone={toneFor(row.status)}>{titleCase(row.status)}</StatusBadge> },
              ]}
            />
          </>
        ) : null}
      </QueryPanel>
    </PageContainer>
  )
}
