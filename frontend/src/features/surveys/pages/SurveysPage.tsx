import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Button } from '@/components/ui/Button'
import { FormField, Select, TextInput } from '@/components/forms/fields'
import { PageContainer, PageHeader } from '@/components/layout/PageHeader'
import { DataTable, StatusBadge } from '@/components/tables/DataTable'
import { PhotoGallery } from '@/features/workspace/PhotoGallery'
import { QueryPanel } from '@/features/workspace/QueryPanel'
import { toneFor } from '@/features/workspace/status'
import { api } from '@/lib/api'
import { titleCase } from '@/utils/format'

type Lead = { id: string; contactName: string }
type Survey = {
  id: string
  leadId: string
  status: string
  scheduledOn: string | null
  roofType: string | null
  monthlyBill: string | null
  photos: { id: string; fileName: string }[]
}

export function SurveysPage() {
  const client = useQueryClient()
  const [leadId, setLeadId] = useState('')
  const [scheduledOn, setScheduledOn] = useState('')
  const [roofType, setRoofType] = useState('RCC')
  const leads = useQuery({ queryKey: ['leads'], queryFn: async () => (await api.get<Lead[]>('/v1/leads')).data })
  const surveys = useQuery({ queryKey: ['surveys'], queryFn: async () => (await api.get<Survey[]>('/v1/surveys')).data })
  const create = useMutation({
    mutationFn: async () =>
      api.post('/v1/surveys', { leadId, status: 'SCHEDULED', scheduledOn: scheduledOn || null, roofType }),
    onSuccess: async () => {
      await client.invalidateQueries({ queryKey: ['surveys'] })
    },
  })
  const upload = useMutation({
    mutationFn: async ({ id, file }: { id: string; file: File }) => {
      const body = new FormData()
      body.append('file', file)
      await api.post(`/v1/surveys/${id}/photos`, body)
    },
    onSuccess: async () => client.invalidateQueries({ queryKey: ['surveys'] }),
  })
  const names = new Map((leads.data ?? []).map((lead) => [lead.id, lead.contactName]))

  return (
    <PageContainer>
      <PageHeader title="Surveys" description="Roof details captured before a quotation is priced." />
      <form
        className="grid gap-4 rounded-card border border-line bg-surface p-5 md:grid-cols-4"
        onSubmit={(event) => {
          event.preventDefault()
          create.mutate()
        }}
      >
        <FormField label="Lead" htmlFor="survey-lead">
          <Select id="survey-lead" value={leadId} onChange={(event) => setLeadId(event.target.value)} required>
            <option value="">Select a lead</option>
            {(leads.data ?? []).map((lead) => (
              <option key={lead.id} value={lead.id}>
                {lead.contactName}
              </option>
            ))}
          </Select>
        </FormField>
        <FormField label="Visit date" htmlFor="survey-date">
          <TextInput id="survey-date" type="date" value={scheduledOn} onChange={(event) => setScheduledOn(event.target.value)} />
        </FormField>
        <FormField label="Roof" htmlFor="survey-roof">
          <TextInput id="survey-roof" value={roofType} onChange={(event) => setRoofType(event.target.value)} />
        </FormField>
        <div className="flex items-end">
          <Button type="submit" disabled={!leadId || create.isPending}>
            Schedule survey
          </Button>
        </div>
      </form>
      <QueryPanel
        isLoading={surveys.isLoading}
        isError={surveys.isError}
        onRetry={() => void surveys.refetch()}
        isEmpty={(surveys.data?.length ?? 0) === 0}
        empty={{ title: 'No site visits', description: 'Schedule a survey from an open lead.' }}
      >
        <DataTable
          rows={surveys.data ?? []}
          getRowId={(row) => row.id}
          empty={null}
          columns={[
            { id: 'lead', header: 'Lead', cell: (row) => names.get(row.leadId) ?? 'Lead' },
            { id: 'date', header: 'Date', cell: (row) => row.scheduledOn ?? '—' },
            { id: 'roof', header: 'Roof', cell: (row) => row.roofType ?? '—' },
            { id: 'status', header: 'Status', cell: (row) => <StatusBadge tone={toneFor(row.status)}>{titleCase(row.status)}</StatusBadge> },
            {
              id: 'photo',
              header: 'Photo',
              cell: (row) => (
                <div className="flex flex-col gap-2">
                  <input
                    aria-label={`Upload photo for ${names.get(row.leadId) ?? 'survey'}`}
                    type="file"
                    accept="image/png,image/jpeg"
                    onChange={(event) => {
                      const file = event.target.files?.[0]
                      if (file) upload.mutate({ id: row.id, file })
                    }}
                  />
                  <PhotoGallery
                    photos={(row.photos ?? []).map((photo) => ({
                      id: photo.id,
                      name: photo.fileName,
                      path: `/v1/surveys/${row.id}/photos/${photo.id}`,
                    }))}
                  />
                </div>
              ),
            },
          ]}
        />
      </QueryPanel>
    </PageContainer>
  )
}
