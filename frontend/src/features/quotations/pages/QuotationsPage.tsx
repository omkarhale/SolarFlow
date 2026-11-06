import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Button } from '@/components/ui/Button'
import { FormField, TextInput } from '@/components/forms/fields'
import { PageContainer, PageHeader } from '@/components/layout/PageHeader'
import { DataTable, StatusBadge } from '@/components/tables/DataTable'
import { downloadFile } from '@/features/workspace/download'
import { QueryPanel } from '@/features/workspace/QueryPanel'
import { toneFor } from '@/features/workspace/status'
import { api } from '@/lib/api'
import { inr, titleCase } from '@/utils/format'

type Quote = {
  id: string
  number: string
  status: string
  systemCapacityKw: string | null
  subtotal: string
  discountAmount: string
  taxAmount: string
  totalAmount: string
}
type Preview = { subtotal: string; discountAmount: string; taxAmount: string; total: string }

export function QuotationsPage() {
  const client = useQueryClient()
  const [description, setDescription] = useState('540 W mono panel')
  const [quantity, setQuantity] = useState('6')
  const [unitPrice, setUnitPrice] = useState('14500')
  const [discount, setDiscount] = useState('0')
  const [taxRate, setTaxRate] = useState('18')
  const [preview, setPreview] = useState<Preview | null>(null)
  const quotes = useQuery({ queryKey: ['quotations'], queryFn: async () => (await api.get<Quote[]>('/v1/quotations')).data })
  const payload = () => ({
    discountAmount: discount,
    taxRate,
    systemCapacityKw: (Number(quantity) * 0.54).toFixed(2),
    panelCount: Number(quantity),
    lines: [{ description, quantity, unitPrice }],
  })
  const previewQuote = useMutation({
    mutationFn: async () => (await api.post<Preview>('/v1/quotations/preview', payload())).data,
    onSuccess: setPreview,
  })
  const create = useMutation({
    mutationFn: async () => api.post('/v1/quotations', payload()),
    onSuccess: async () => {
      setPreview(null)
      await client.invalidateQueries({ queryKey: ['quotations'] })
    },
  })
  const accept = useMutation({
    mutationFn: async (id: string) => api.post(`/v1/quotations/${id}/accept`),
    onSuccess: async () => {
      await client.invalidateQueries({ queryKey: ['quotations'] })
      await client.invalidateQueries({ queryKey: ['projects'] })
    },
  })

  return (
    <PageContainer>
      <PageHeader title="Quotations" description="Price a system, preview tax, then issue a branded PDF." />
      <form
        className="grid gap-4 rounded-card border border-line bg-surface p-5 md:grid-cols-3"
        onSubmit={(event) => {
          event.preventDefault()
          create.mutate()
        }}
      >
        <FormField label="Line" htmlFor="quote-line">
          <TextInput id="quote-line" value={description} onChange={(event) => setDescription(event.target.value)} />
        </FormField>
        <FormField label="Quantity" htmlFor="quote-qty">
          <TextInput id="quote-qty" value={quantity} onChange={(event) => setQuantity(event.target.value)} />
        </FormField>
        <FormField label="Unit price" htmlFor="quote-price">
          <TextInput id="quote-price" value={unitPrice} onChange={(event) => setUnitPrice(event.target.value)} />
        </FormField>
        <FormField label="Discount" htmlFor="quote-discount">
          <TextInput id="quote-discount" value={discount} onChange={(event) => setDiscount(event.target.value)} />
        </FormField>
        <FormField label="GST %" htmlFor="quote-tax">
          <TextInput id="quote-tax" value={taxRate} onChange={(event) => setTaxRate(event.target.value)} />
        </FormField>
        <div className="flex items-end gap-2">
          <Button type="button" variant="secondary" onClick={() => previewQuote.mutate()}>
            Preview
          </Button>
          <Button type="submit" disabled={create.isPending}>
            Create draft
          </Button>
        </div>
        {preview ? (
          <p className="md:col-span-3 text-body text-ink">
            Subtotal {inr(preview.subtotal)} · Discount {inr(preview.discountAmount)} · GST {inr(preview.taxAmount)} · Total {inr(preview.total)}
          </p>
        ) : null}
      </form>
      <QueryPanel
        isLoading={quotes.isLoading}
        isError={quotes.isError}
        onRetry={() => void quotes.refetch()}
        isEmpty={(quotes.data?.length ?? 0) === 0}
        empty={{ title: 'No quotations', description: 'Create a draft after the site survey is complete.' }}
      >
        <DataTable
          rows={quotes.data ?? []}
          getRowId={(row) => row.id}
          empty={null}
          columns={[
            { id: 'number', header: 'Number', cell: (row) => row.number },
            { id: 'kw', header: 'kW', cell: (row) => row.systemCapacityKw ?? '—' },
            { id: 'total', header: 'Total', cell: (row) => inr(row.totalAmount) },
            { id: 'status', header: 'Status', cell: (row) => <StatusBadge tone={toneFor(row.status)}>{titleCase(row.status)}</StatusBadge> },
            {
              id: 'actions',
              header: 'Actions',
              cell: (row) => (
                <div className="flex gap-2">
                  <Button size="sm" variant="secondary" onClick={() => void downloadFile(`/v1/quotations/${row.id}/pdf`, `${row.number}.pdf`)}>
                    PDF
                  </Button>
                  {row.status !== 'ACCEPTED' ? (
                    <Button size="sm" onClick={() => accept.mutate(row.id)}>
                      Accept
                    </Button>
                  ) : null}
                </div>
              ),
            },
          ]}
        />
      </QueryPanel>
    </PageContainer>
  )
}
