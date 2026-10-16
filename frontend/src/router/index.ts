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
    meta: {
      public: true,
    },
  },
  {
    path: '/',
    name: 'dashboard',
    component: () => import('@/views/dashboard/DashboardView.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  /*{
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: {
      public: true,
    },
  },*/
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
  if (to.meta.requiresAuth && !authStore.autenticado) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    }
  }
  if (to.name === 'login' && authStore.autenticado) {
    return { name: 'dashboard' }
  }
  return true
})

export default router
