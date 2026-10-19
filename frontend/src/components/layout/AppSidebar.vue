<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  BookOpen,
  Building2,
  ClipboardList,
  GraduationCap,
  LayoutDashboard,
  LogOut,
  UserRound,
  UsersRound,
  ClipboardCheck,
  DoorOpen,
  UserCog,
} from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()

const usuario = computed(() => authStore.usuario)

const menu = [
  { nombre: 'Dashboard', ruta: '/', icono: LayoutDashboard },
  { nombre: 'Personal', ruta: '/personal', icono: UsersRound },
  { nombre: 'Aulas', ruta: '/aulas', icono: Building2 },
  { nombre: 'Capacitaciones', ruta: '/capacitaciones', icono: BookOpen },
  { nombre: 'Asignación de aulas', ruta: '/asignacion-aulas', icono: ClipboardCheck },
  { nombre: 'Asignación de tutores', ruta: '/asignacion-tutores', icono: UserCog },
  { nombre: 'Asignación de participantes', ruta: '/asignacion-participantes', icono: DoorOpen },
  { nombre: 'Participantes', ruta: '/participantes', icono: ClipboardList },
]

function cerrarSesion() {
  authStore.cerrarSesion()
  router.replace({ name: 'login' })
}
</script>

<template>
  <aside class="flex h-full w-64 flex-col bg-[#123f3b] text-white">
    <!-- Logo -->
    <div class="flex items-center gap-3 border-b border-white/10 px-5 py-6">
      <div class="flex h-11 w-11 items-center justify-center rounded-xl bg-[#17665e]">
        <GraduationCap :size="25" />
      </div>

      <div>
        <h1 class="text-lg font-bold">EDA 2026</h1>
        <p class="text-xs text-[#b7d9d2]">Gestión de evaluación</p>
      </div>
    </div>

    <!-- Navegación -->
    <nav class="flex-1 space-y-1 overflow-y-auto px-3 py-5">
      <p class="mb-3 px-3 text-xs font-semibold uppercase tracking-wider text-[#8fbab1]">
        Menú principal
      </p>

      <RouterLink
        v-for="item in menu"
        :key="item.ruta"
        :to="item.ruta"
        class="flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium text-[#c5e2dc] transition hover:bg-white/10 hover:text-white"
        active-class="bg-[#17665e] text-white"
        exact-active-class="bg-[#17665e] text-white"
      >
        <component :is="item.icono" :size="19" />
        <span>{{ item.nombre }}</span>
      </RouterLink>
    </nav>

    <!-- Usuario -->
    <div class="border-t border-white/10 p-4">
      <div class="mb-4 flex items-center gap-3">
        <div class="flex h-10 w-10 items-center justify-center rounded-full bg-[#17665e]">
          <UserRound :size="20" />
        </div>

        <div class="min-w-0">
          <p class="truncate text-sm font-semibold">
            {{ usuario?.nombres }} {{ usuario?.apellidos }}
          </p>
          <p class="text-xs text-[#b7d9d2]">
            {{ usuario?.rol }}
          </p>
        </div>
      </div>

      <button
        type="button"
        class="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm text-[#c5e2dc] transition hover:bg-red-500/20 hover:text-white"
        @click="cerrarSesion"
      >
        <LogOut :size="19" />
        Cerrar sesión
      </button>
    </div>
  </aside>
</template>
