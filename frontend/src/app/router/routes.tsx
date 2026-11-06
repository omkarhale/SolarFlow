import { Navigate, createBrowserRouter } from 'react-router-dom'
import { AppLayout } from '@/app/layouts/AppLayout'
import { LoginPage } from '@/features/auth/pages/LoginPage'
import { RequireAuth } from '@/features/auth/RequireAuth'
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage'
import { CustomerDetailPage } from '@/features/customers/pages/CustomerDetailPage'
import { CustomersPage } from '@/features/customers/pages/CustomersPage'
import { LeadsPage } from '@/features/leads/pages/LeadsPage'
import { SurveysPage } from '@/features/surveys/pages/SurveysPage'
import { QuotationsPage } from '@/features/quotations/pages/QuotationsPage'
import { ProjectDetailPage, ProjectsPage } from '@/features/projects/pages/ProjectsPage'
import { PaymentsPage } from '@/features/payments/pages/PaymentsPage'
import { InventoryPage } from '@/features/inventory/pages/InventoryPage'
import { ServicePage } from '@/features/services/pages/ServicePage'
import { ReportsPage } from '@/features/reports/pages/ReportsPage'
import { TeamPage } from '@/features/team/pages/TeamPage'
import { SettingsPage } from '@/features/settings/pages/SettingsPage'

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    path: '/',
    element: <RequireAuth />,
    children: [
      {
        element: <AppLayout />,
        children: [
      { index: true, element: <DashboardPage /> },
      { path: 'customers', element: <CustomersPage /> },
      { path: 'customers/:customerId', element: <CustomerDetailPage /> },
      { path: 'leads', element: <LeadsPage /> },
      { path: 'surveys', element: <SurveysPage /> },
      { path: 'quotations', element: <QuotationsPage /> },
      { path: 'projects', element: <ProjectsPage /> },
      { path: 'projects/:projectId', element: <ProjectDetailPage /> },
      { path: 'payments', element: <PaymentsPage /> },
      { path: 'inventory', element: <InventoryPage /> },
      { path: 'service', element: <ServicePage /> },
      { path: 'reports', element: <ReportsPage /> },
      { path: 'team', element: <TeamPage /> },
      { path: 'settings', element: <SettingsPage /> },
      { path: '*', element: <Navigate to="/" replace /> },
        ],
      },
    ],
  },
])
