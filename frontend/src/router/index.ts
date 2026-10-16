import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    requiresAuth?: boolean
  }
}

const routes: RouteRecordRaw[] = [
  // Página pública de inicio de sesión
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true },
  },

  // Páginas privadas con menú lateral
  {
    path: '/',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
      },
      {
        path: 'personal',
        name: 'personal',
        component: () => import('@/views/personal/PersonalView.vue'),
        props: { titulo: 'Personal' },
      },
      {
        path: 'aulas',
        name: 'aulas',
        component: () => import('@/views/ModulePlaceholderView.vue'),
        props: { titulo: 'Aulas' },
      },
      {
        path: 'capacitaciones',
        name: 'capacitaciones',
        component: () => import('@/views/ModulePlaceholderView.vue'),
        props: { titulo: 'Capacitaciones' },
      },
      {
        path: 'participantes',
        name: 'participantes',
        component: () => import('@/views/ModulePlaceholderView.vue'),
        props: { titulo: 'Participantes' },
      },
      {
        path: 'reportes',
        name: 'reportes',
        component: () => import('@/views/ModulePlaceholderView.vue'),
        props: { titulo: 'Reportes' },
      },
    ],
  },
  // Página no encontrada
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { public: true },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  // Eliminar una sesión vencida
  if (authStore.accessToken && !authStore.autenticado) {
    authStore.cerrarSesion()
  }

  // Proteger las páginas privadas
  if (to.matched.some((record) => record.meta.requiresAuth)) {
    if (!authStore.autenticado) {
      return {
        name: 'login',
        query: { redirect: to.fullPath },
      }
    }
  }

  // Evitar que un usuario autenticado vuelva al login
  if (to.name === 'login' && authStore.autenticado) {
    return { name: 'dashboard' }
  }

  return true
})

export default router
