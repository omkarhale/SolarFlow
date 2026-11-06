import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Button } from '@/components/ui/Button'
import { FormField, Select, TextInput } from '@/components/forms/fields'
import { StatCard } from '@/components/charts/ChartCard'
import { PageContainer, PageHeader } from '@/components/layout/PageHeader'
import { DataTable, StatusBadge } from '@/components/tables/DataTable'
import { downloadFile } from '@/features/workspace/download'
import { QueryPanel } from '@/features/workspace/QueryPanel'
import { toneFor } from '@/features/workspace/status'
import { api } from '@/lib/api'
import { inr, titleCase } from '@/utils/format'

type Customer = { id: string; displayName: string }
type Payment = { id: string; customerId: string; amount: string; method: string; status: string; receiptNumber: string | null }
type Page = { received: string; pending: string; outstanding: string; items: Payment[] }

export function PaymentsPage() {
  const client = useQueryClient()
  const [customerId, setCustomerId] = useState('')
  const [amount, setAmount] = useState('')
  const [method, setMethod] = useState('UPI')
  const customers = useQuery({
    queryKey: ['customers', 'options'],
    queryFn: async () => (await api.get<{ items: Customer[] }>('/v1/customers?size=100')).data.items,
  })
  const payments = useQuery({ queryKey: ['payments'], queryFn: async () => (await api.get<Page>('/v1/payments')).data })
  const names = new Map((customers.data ?? []).map((customer) => [customer.id, customer.displayName]))
  const create = useMutation({
    mutationFn: async () => api.post('/v1/payments', { customerId, amount, method, status: 'RECEIVED' }),
    onSuccess: async () => {
      setAmount('')
      await client.invalidateQueries({ queryKey: ['payments'] })
      await client.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })

  return (
    <PageContainer>
      <PageHeader title="Payments" description="Collections against accepted work, with a receipt for every received payment." />
      {payments.data ? (
        <section className="grid gap-4 sm:grid-cols-3">
          <StatCard label="Received" value={inr(payments.data.received)} />
          <StatCard label="Pending" value={inr(payments.data.pending)} />
          <StatCard label="Outstanding" value={inr(payments.data.outstanding)} />
        </section>
      ) : null}
      <form
        className="grid gap-4 rounded-card border border-line bg-surface p-5 md:grid-cols-4"
        onSubmit={(event) => {
          event.preventDefault()
          create.mutate()
        }}
      >
        <FormField label="Customer" htmlFor="payment-customer">
          <Select id="payment-customer" value={customerId} onChange={(event) => setCustomerId(event.target.value)} required>
            <option value="">Select a customer</option>
            {(customers.data ?? []).map((customer) => (
              <option key={customer.id} value={customer.id}>
                {customer.displayName}
              </option>
            ))}
          </Select>
        </FormField>
        <FormField label="Amount" htmlFor="payment-amount">
          <TextInput id="payment-amount" value={amount} onChange={(event) => setAmount(event.target.value)} required />
        </FormField>
        <FormField label="Method" htmlFor="payment-method">
          <Select id="payment-method" value={method} onChange={(event) => setMethod(event.target.value)}>
            {['UPI', 'CASH', 'BANK_TRANSFER', 'CHEQUE', 'CARD'].map((option) => (
              <option key={option} value={option}>
                {titleCase(option)}
              </option>
            ))}
          </Select>
        </FormField>
        <div className="flex items-end">
          <Button type="submit" disabled={!customerId || !amount}>
            Record receipt
          </Button>
        </div>
      </form>
      <QueryPanel
        isLoading={payments.isLoading}
        isError={payments.isError}
        onRetry={() => void payments.refetch()}
        isEmpty={(payments.data?.items.length ?? 0) === 0}
        empty={{ title: 'No payments', description: 'Record the first collection against a customer.' }}
      >
        <DataTable
          rows={payments.data?.items ?? []}
          getRowId={(row) => row.id}
          empty={null}
          columns={[
            { id: 'customer', header: 'Customer', cell: (row) => names.get(row.customerId) ?? 'Customer' },
            { id: 'amount', header: 'Amount', cell: (row) => inr(row.amount) },
            { id: 'method', header: 'Method', cell: (row) => titleCase(row.method) },
            { id: 'status', header: 'Status', cell: (row) => <StatusBadge tone={toneFor(row.status)}>{titleCase(row.status)}</StatusBadge> },
            {
              id: 'receipt',
              header: 'Receipt',
              cell: (row) =>
                row.status === 'RECEIVED' ? (
                  <Button size="sm" variant="secondary" onClick={() => void downloadFile(`/v1/payments/${row.id}/receipt`, `${row.receiptNumber ?? 'receipt'}.pdf`)}>
                    PDF
                  </Button>
                ) : (
                  '—'
                ),
            },
          ]}
        />
      </QueryPanel>
    </PageContainer>
  )
}
