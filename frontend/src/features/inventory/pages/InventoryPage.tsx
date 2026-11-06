import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Button } from '@/components/ui/Button'
import { FormField, Select, TextInput } from '@/components/forms/fields'
import { PageContainer, PageHeader } from '@/components/layout/PageHeader'
import { DataTable, StatusBadge } from '@/components/tables/DataTable'
import { QueryPanel } from '@/features/workspace/QueryPanel'
import { api } from '@/lib/api'
import { inr, titleCase } from '@/utils/format'

type Product = {
  id: string
  sku: string
  name: string
  category: string
  unitPrice: string
  stockQuantity: string
  reorderLevel: string
  lowStock: boolean
}

export function InventoryPage() {
  const client = useQueryClient()
  const [sku, setSku] = useState('')
  const [name, setName] = useState('')
  const [price, setPrice] = useState('')
  const [selected, setSelected] = useState('')
  const [quantity, setQuantity] = useState('1')
  const [movementType, setMovementType] = useState('IN')
  const products = useQuery({ queryKey: ['products'], queryFn: async () => (await api.get<Product[]>('/v1/inventory/products')).data })
  const create = useMutation({
    mutationFn: async () =>
      api.post('/v1/inventory/products', {
        sku,
        name,
        category: 'PANEL',
        unit: 'nos',
        unitPrice: price,
        stockQuantity: 0,
        reorderLevel: 2,
        active: true,
      }),
    onSuccess: async () => {
      setSku('')
      setName('')
      setPrice('')
      await client.invalidateQueries({ queryKey: ['products'] })
    },
  })
  const move = useMutation({
    mutationFn: async () => api.post(`/v1/inventory/products/${selected}/movements`, { movementType, quantity }),
    onSuccess: async () => client.invalidateQueries({ queryKey: ['products'] }),
  })

  return (
    <PageContainer>
      <PageHeader title="Inventory" description="Panels, inverters, and the stock movements that keep a site supplied." />
      <form
        className="grid gap-4 rounded-card border border-line bg-surface p-5 md:grid-cols-4"
        onSubmit={(event) => {
          event.preventDefault()
          create.mutate()
        }}
      >
        <FormField label="SKU" htmlFor="sku">
          <TextInput id="sku" value={sku} onChange={(event) => setSku(event.target.value)} required />
        </FormField>
        <FormField label="Name" htmlFor="product-name">
          <TextInput id="product-name" value={name} onChange={(event) => setName(event.target.value)} required />
        </FormField>
        <FormField label="Unit price" htmlFor="product-price">
          <TextInput id="product-price" value={price} onChange={(event) => setPrice(event.target.value)} required />
        </FormField>
        <div className="flex items-end">
          <Button type="submit" disabled={!sku || !name || !price}>
            Add product
          </Button>
        </div>
      </form>
      <form
        className="grid gap-4 rounded-card border border-line bg-surface p-5 md:grid-cols-4"
        onSubmit={(event) => {
          event.preventDefault()
          move.mutate()
        }}
      >
        <FormField label="Product" htmlFor="movement-product">
          <Select id="movement-product" value={selected} onChange={(event) => setSelected(event.target.value)} required>
            <option value="">Select a product</option>
            {(products.data ?? []).map((product) => (
              <option key={product.id} value={product.id}>
                {product.name}
              </option>
            ))}
          </Select>
        </FormField>
        <FormField label="Movement" htmlFor="movement-type">
          <Select id="movement-type" value={movementType} onChange={(event) => setMovementType(event.target.value)}>
            {['IN', 'OUT', 'ADJUSTMENT', 'ALLOCATION'].map((option) => (
              <option key={option} value={option}>
                {titleCase(option)}
              </option>
            ))}
          </Select>
        </FormField>
        <FormField label="Quantity" htmlFor="movement-qty">
          <TextInput id="movement-qty" value={quantity} onChange={(event) => setQuantity(event.target.value)} />
        </FormField>
        <div className="flex items-end">
          <Button type="submit" variant="secondary" disabled={!selected}>
            Record movement
          </Button>
        </div>
      </form>
      <QueryPanel
        isLoading={products.isLoading}
        isError={products.isError}
        onRetry={() => void products.refetch()}
        isEmpty={(products.data?.length ?? 0) === 0}
        empty={{ title: 'No products', description: 'Add the first panel or inverter the company stocks.' }}
      >
        <DataTable
          rows={products.data ?? []}
          getRowId={(row) => row.id}
          empty={null}
          columns={[
            { id: 'sku', header: 'SKU', cell: (row) => row.sku },
            { id: 'name', header: 'Product', cell: (row) => row.name },
            { id: 'category', header: 'Category', cell: (row) => titleCase(row.category) },
            { id: 'price', header: 'Price', cell: (row) => inr(row.unitPrice) },
            { id: 'stock', header: 'Stock', cell: (row) => row.stockQuantity },
            {
              id: 'low',
              header: 'Reorder',
              cell: (row) => (row.lowStock ? <StatusBadge tone="warning">Low</StatusBadge> : <StatusBadge tone="success">OK</StatusBadge>),
            },
          ]}
        />
      </QueryPanel>
    </PageContainer>
  )
}
