# Design system

SolarFlow is a light, quiet workspace. Navy is the structural color. Green is used for the active state and positive status, not as a fill for large areas.

Tokens live in two places that must stay aligned:

- `frontend/src/styles/globals.css` for CSS variables
- `frontend/tailwind.config.ts` for Tailwind utilities

## Color

| Token | Value | Use |
| --- | --- | --- |
| Navy | `#14243A` | Primary actions, sidebar mark, focus |
| Green | `#16A36A` | Active navigation, success |
| Green soft | `#E8F7EF` | Active navigation background |
| Canvas | `#F7F9FC` | Application background |
| Surface | `#FFFFFF` | Cards, sidebar, dialogs |
| Surface muted | `#F1F4F8` | Toolbars, table headers |
| Ink | `#182230` | Primary text |
| Ink secondary | `#667085` | Supporting text |
| Ink muted | `#98A2B3` | Placeholders, metadata |
| Line | `#E5EAF0` | Borders |
| Warning | `#E6A23C` | Pending or expiring |
| Danger | `#DC5454` | Destructive and failed |
| Info | `#4385DB` | Informational |

## Type

Inter is self-hosted. The scale is `display`, `title`, `card`, `body`, `secondary`, `label`, and `meta` in the Tailwind config. Body copy is 14px. Page titles are 28px with tight tracking.

## Space and shape

Spacing follows Tailwind's 4px scale. Controls are 40px tall, 32px when compact. Cards use a 12px radius and a 1px border. Shadows stay at `shadow-card` or `shadow-pop` for menus.

## Components

Layout: `AppLayout`, `Sidebar`, `SidebarItem`, `TopNavbar`, `PageHeader`, `PageContainer`, `ResponsiveDrawer`.

Forms live in `components/forms/fields.tsx`. Tables and status live in `components/tables/DataTable.tsx`. Charts use `ChartCard` and `StatCard`. Feedback lives in `components/feedback`. Navigation helpers live in `components/common/navigation.tsx`.

Buttons, inputs, and menus expose accessible names, use `focus-visible`, and keep a 40px target on primary controls.

The workspace has eight light themes and four dark themes: Solar Navy, Harbor, Dune, Slate, Indigo Desk, Evergreen, Aurora, Lagoon, Midnight, Obsidian, Dusk, and Ember. A custom theme stores primary, accent, background, surface, and text in the browser under `solarflow.theme`. On a dark background, status chips switch to dark tints so they stay readable.

Components read semantic Tailwind colors, and those colors point at the CSS variables in `globals.css`. Do not put a new hex value in a component.
