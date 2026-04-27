import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

export default createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'tasklistLight',
    themes: {
      tasklistLight: {
        dark: false,
        colors: {
          background: '#f3f5fb',
          surface: '#ffffff',
          primary: '#4f46e5',
          secondary: '#0891b2',
          success: '#16a34a',
          warning: '#d97706',
          error: '#dc2626',
          info: '#2563eb',
          'on-background': '#0f172a',
          'on-surface': '#0f172a',
          'surface-variant': '#eef2ff',
          'on-surface-variant': '#475569',
          outline: '#d6dbee',
        },
      },
      tasklistDark: {
        dark: true,
        colors: {
          background: '#0b1120',
          surface: '#111827',
          primary: '#818cf8',
          secondary: '#22d3ee',
          success: '#22c55e',
          warning: '#f59e0b',
          error: '#f87171',
          info: '#60a5fa',
          'on-background': '#e2e8f0',
          'on-surface': '#e2e8f0',
          'surface-variant': '#1f2937',
          'on-surface-variant': '#94a3b8',
          outline: '#334155',
        },
      },
    },
  },
  defaults: {
    global: {
      ripple: true,
    },
    VCard: {
      rounded: 'xl',
      elevation: 0,
    },
    VTextField: {
      variant: 'outlined',
      density: 'comfortable',
      color: 'primary',
      hideDetails: 'auto',
      rounded: 'lg',
    },
    VTextarea: {
      variant: 'outlined',
      density: 'comfortable',
      color: 'primary',
      hideDetails: 'auto',
      rounded: 'lg',
    },
    VSelect: {
      variant: 'outlined',
      density: 'comfortable',
      color: 'primary',
      hideDetails: 'auto',
      rounded: 'lg',
    },
    VBtn: {
      rounded: 'lg',
      elevation: 0,
      style: 'text-transform:none;font-weight:600;letter-spacing:0;',
    },
    VDialog: {
      scrim: 'rgba(15, 23, 42, 0.55)',
    },
  },
})
