import { useQuery } from '@tanstack/react-query'
import { ChartCard, StatCard } from '@/components/charts/ChartCard'
import { PageContainer, PageHeader } from '@/components/layout/PageHeader'
import { DataTable, StatusBadge } from '@/components/tables/DataTable'
import { QueryPanel } from '@/features/workspace/QueryPanel'
import { toneFor } from '@/features/workspace/status'
import { api } from '@/lib/api'
import { inr, titleCase } from '@/utils/format'

type Report = {
  kpis: { openLeads: number; pendingQuotations: number; activeProjects: number; outstanding: string; collectedThisMonth: string }
  installationProgress: { id: string; name: string; progress: number; status: string }[]
  pendingPayments: { id: string; amount: string; status: string }[]
}

export function ReportsPage() {
  const report = useQuery({ queryKey: ['reports'], queryFn: async () => (await api.get<Report>('/v1/reports')).data })
  return (
    <PageContainer>
      <PageHeader title="Reports" description="A longer view of pipeline, installations, and money still to collect." />
      <QueryPanel isLoading={report.isLoading} isError={report.isError} onRetry={() => void report.refetch()}>
        {report.data ? (
          <>
            <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
              <StatCard label="Open leads" value={String(report.data.kpis.openLeads)} />
              <StatCard label="Pending quotations" value={String(report.data.kpis.pendingQuotations)} />
              <StatCard label="Active projects" value={String(report.data.kpis.activeProjects)} />
              <StatCard label="Collected this month" value={inr(report.data.kpis.collectedThisMonth)} />
              <StatCard label="Outstanding" value={inr(report.data.kpis.outstanding)} />
            </section>
            <ChartCard title="Installation progress" description="Share of each live project that is complete.">
              <DataTable
                rows={report.data.installationProgress}
                getRowId={(row) => row.id}
                empty={<p className="text-secondary text-ink-secondary">No active installations.</p>}
                columns={[
                  { id: 'name', header: 'Project', cell: (row) => row.name },
                  { id: 'progress', header: 'Progress', cell: (row) => `${row.progress}%` },
                  { id: 'status', header: 'Status', cell: (row) => <StatusBadge tone={toneFor(row.status)}>{titleCase(row.status)}</StatusBadge> },
                ]}
              />
            </ChartCard>
          </>
        ) : null}
      </QueryPanel>
    </PageContainer>
  )
}
