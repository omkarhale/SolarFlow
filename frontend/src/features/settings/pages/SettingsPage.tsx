import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/Button'
import { FormField, TextInput } from '@/components/forms/fields'
import { PageContainer, PageHeader } from '@/components/layout/PageHeader'
import { ThemeSettings } from '@/features/settings/components/ThemeSettings'
import { api } from '@/lib/api'

type Company = {
  name: string
  legalName: string | null
  gstin: string | null
  email: string | null
  phone: string | null
  addressLine: string | null
  city: string | null
  state: string | null
  pincode: string | null
}

export function SettingsPage() {
  const client = useQueryClient()
  const company = useQuery({ queryKey: ['company'], queryFn: async () => (await api.get<Company>('/v1/company')).data })
  const [form, setForm] = useState<Company | null>(null)
  useEffect(() => {
    if (company.data) setForm(company.data)
  }, [company.data])
  const save = useMutation({
    mutationFn: async (value: Company) => api.patch('/v1/company', value),
    onSuccess: async () => client.invalidateQueries({ queryKey: ['company'] }),
  })

  function update(key: keyof Company, value: string) {
    setForm((current) => (current ? { ...current, [key]: value } : current))
  }

  return (
    <PageContainer>
      <PageHeader title="Settings" description="Appearance for this browser, and the company details printed on quotations." />
      <ThemeSettings />
      {form ? (
        <form
          className="grid gap-4 rounded-card border border-line bg-surface p-5 md:grid-cols-2"
          onSubmit={(event) => {
            event.preventDefault()
            save.mutate(form)
          }}
        >
          <FormField label="Company name" htmlFor="company-name">
            <TextInput id="company-name" value={form.name} onChange={(event) => update('name', event.target.value)} required />
          </FormField>
          <FormField label="Legal name" htmlFor="company-legal">
            <TextInput id="company-legal" value={form.legalName ?? ''} onChange={(event) => update('legalName', event.target.value)} />
          </FormField>
          <FormField label="GSTIN" htmlFor="company-gstin">
            <TextInput id="company-gstin" value={form.gstin ?? ''} onChange={(event) => update('gstin', event.target.value)} />
          </FormField>
          <FormField label="City" htmlFor="company-city">
            <TextInput id="company-city" value={form.city ?? ''} onChange={(event) => update('city', event.target.value)} />
          </FormField>
          <FormField label="State" htmlFor="company-state">
            <TextInput id="company-state" value={form.state ?? ''} onChange={(event) => update('state', event.target.value)} />
          </FormField>
          <FormField label="Phone" htmlFor="company-phone">
            <TextInput id="company-phone" value={form.phone ?? ''} onChange={(event) => update('phone', event.target.value)} />
          </FormField>
          <div className="md:col-span-2">
            <Button type="submit" disabled={save.isPending}>
              {save.isPending ? 'Saving' : 'Save company'}
            </Button>
            {save.isError ? <p className="mt-2 text-meta text-danger">Only an owner or admin can update the company profile.</p> : null}
            {save.isSuccess ? <p className="mt-2 text-meta text-green-ink">Company profile saved.</p> : null}
          </div>
        </form>
      ) : null}
    </PageContainer>
  )
}
