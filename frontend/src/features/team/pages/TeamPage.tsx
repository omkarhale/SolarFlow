import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Button } from '@/components/ui/Button'
import { FormField, Select, TextInput } from '@/components/forms/fields'
import { PageContainer, PageHeader } from '@/components/layout/PageHeader'
import { DataTable, StatusBadge } from '@/components/tables/DataTable'
import { QueryPanel } from '@/features/workspace/QueryPanel'
import { api, type UserProfile } from '@/lib/api'
import { titleCase } from '@/utils/format'

export function TeamPage() {
  const client = useQueryClient()
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [email, setEmail] = useState('')
  const [role, setRole] = useState('SALES')
  const [password, setPassword] = useState('')
  const team = useQuery({ queryKey: ['team'], queryFn: async () => (await api.get<UserProfile[]>('/v1/team')).data })
  const create = useMutation({
    mutationFn: async () => api.post('/v1/team', { firstName, lastName, email, role, password }),
    onSuccess: async () => {
      setFirstName('')
      setLastName('')
      setEmail('')
      setPassword('')
      await client.invalidateQueries({ queryKey: ['team'] })
    },
  })

  return (
    <PageContainer>
      <PageHeader title="Team" description="People who can sign in to this company. Owners and admins can add members." />
      <form
        className="grid gap-4 rounded-card border border-line bg-surface p-5 md:grid-cols-3"
        onSubmit={(event) => {
          event.preventDefault()
          create.mutate()
        }}
      >
        <FormField label="First name" htmlFor="member-first">
          <TextInput id="member-first" value={firstName} onChange={(event) => setFirstName(event.target.value)} required />
        </FormField>
        <FormField label="Last name" htmlFor="member-last">
          <TextInput id="member-last" value={lastName} onChange={(event) => setLastName(event.target.value)} required />
        </FormField>
        <FormField label="Email" htmlFor="member-email">
          <TextInput id="member-email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
        </FormField>
        <FormField label="Role" htmlFor="member-role">
          <Select id="member-role" value={role} onChange={(event) => setRole(event.target.value)}>
            {['ADMIN', 'SALES', 'OPERATIONS', 'TECHNICIAN'].map((option) => (
              <option key={option} value={option}>
                {titleCase(option)}
              </option>
            ))}
          </Select>
        </FormField>
        <FormField label="Temporary password" htmlFor="member-password">
          <TextInput id="member-password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} required minLength={10} />
        </FormField>
        <div className="flex items-end">
          <Button type="submit" disabled={password.length < 10}>
            Add member
          </Button>
        </div>
        {create.isError ? <p className="text-meta text-danger md:col-span-3">The member could not be added. Check that you are an owner or admin.</p> : null}
      </form>
      <QueryPanel
        isLoading={team.isLoading}
        isError={team.isError}
        onRetry={() => void team.refetch()}
        isEmpty={(team.data?.length ?? 0) === 0}
        empty={{ title: 'No teammates', description: 'The signed-in company has no active users yet.' }}
      >
        <DataTable
          rows={team.data ?? []}
          getRowId={(row) => row.id}
          empty={null}
          columns={[
            { id: 'name', header: 'Name', cell: (row) => `${row.firstName} ${row.lastName}` },
            { id: 'email', header: 'Email', cell: (row) => row.email },
            { id: 'role', header: 'Role', cell: (row) => <StatusBadge tone="info">{titleCase(row.role)}</StatusBadge> },
            { id: 'status', header: 'Status', cell: (row) => titleCase(row.status) },
          ]}
        />
      </QueryPanel>
    </PageContainer>
  )
}
