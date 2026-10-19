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
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true },
  },
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
        path: 'personal/:id',
        name: 'personal-detalle',
        component: () => import('@/views/personal/PersonalDetalleView.vue'),
      },
      {
        path: 'aulas',
        name: 'aulas',
        component: () => import('@/views/aula/AulasView.vue'),
      },
      {
        path: 'capacitaciones',
        name: 'capacitaciones',
        component: () => import('@/views/capacitacion/CapacitacionView.vue'),
      },
      {
        path: 'asignacion-tutores',
        name: 'asignacion-tutores',
        component: () => import('@/views/capacitacion/AsignacionTutoresView.vue'),
      },
      {
        path: 'asignacion-participantes',
        name: 'asignacion-participantes',
        component: () => import('@/views/capacitacion/AsignacionParticipantesView.vue'),
      },
      {
        path: '/asignacion-aulas',
        name: 'asignacion-aulas',
        component: () => import('@/views/aula/AulaCapacitacionView.vue'),
      },
      {
        path: '/participantes',
        name: 'participantes',
        component: () => import('@/views/reporte/ParticipantesView.vue'),
      },
    ],
  },
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
  if (authStore.accessToken && !authStore.autenticado) {
    authStore.cerrarSesion()
  }
  if (to.matched.some((record) => record.meta.requiresAuth)) {
    if (!authStore.autenticado) {
      return {
        name: 'login',
        query: { redirect: to.fullPath },
      }
    }
  }
  if (to.name === 'login' && authStore.autenticado) {
    return { name: 'dashboard' }
  }
  return true
})

export default router
