<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  ArrowRight,
  BookOpen,
  Building2,
  CalendarDays,
  ChartColumnIncreasing,
  ClipboardList,
  LayoutDashboard,
  ShieldCheck,
  UsersRound,
} from 'lucide-vue-next'

const authStore = useAuthStore()

const nombreRol = computed(() => {
  const roles: Record<string, string> = {
    CTS: 'Coordinador Técnico de Sede',
    CS: 'Coordinador de Sede',
    CAS: 'Coordinador de Aplicación de Sede',
  }

  return roles[authStore.rol ?? ''] ?? 'Usuario del sistema'
})

const modulos = [
  {
    titulo: 'Personal',
    descripcion: 'Gestión del personal de nivel 2 y nivel 3.',
    icono: UsersRound,
    ruta: '/personal',
  },
  {
    titulo: 'Aulas',
    descripcion: 'Creación y administración de aulas.',
    icono: Building2,
    ruta: '/aulas',
  },
  {
    titulo: 'Capacitaciones',
    descripcion: 'Organización y seguimiento de capacitaciones.',
    icono: BookOpen,
    ruta: '/capacitaciones',
  },
  {
    titulo: 'Participantes',
    descripcion: 'Consulta y administración de participantes.',
    icono: ClipboardList,
    ruta: '/participantes',
  },
  {
    titulo: 'Reportes',
    descripcion: 'Consulta y generación de reportes.',
    icono: ChartColumnIncreasing,
    ruta: '/reportes',
  },
]
</script>

<template>
  <div class="min-h-screen bg-[#edf5f3]">
    <div class="mx-auto max-w-7xl px-5 py-8 sm:px-8">
      <!-- Encabezado -->
      <div class="mb-8">
        <div class="mb-3 flex items-center gap-2 text-sm text-[#17665e]">
          <LayoutDashboard :size="18" />
          Panel principal
        </div>

        <h1 class="text-2xl font-bold text-[#123f3b] sm:text-3xl">
          Dashboard
        </h1>

        <p class="mt-2 text-sm text-slate-500">
          Resumen general del sistema EDA 2026.
        </p>
      </div>

      <!-- Bienvenida -->
      <section
        class="relative overflow-hidden rounded-3xl bg-[#123f3b] px-7 py-9 text-white shadow-lg shadow-[#123f3b]/10 sm:px-10"
      >
        <div
          class="pointer-events-none absolute -right-16 -top-20 h-72 w-72 rounded-full bg-[#2c9d8d]/20"
        ></div>

        <div class="relative z-10">
          <div
            class="mb-5 inline-flex items-center gap-2 rounded-full border border-white/20 bg-white/10 px-3 py-1.5 text-xs font-medium text-[#d7f6ed]"
          >
            <ShieldCheck :size="15" />
            Sesión activa
          </div>

          <h2 class="text-2xl font-bold sm:text-3xl">
            ¡Bienvenido, {{ authStore.usuario?.nombres }}!
          </h2>

          <p class="mt-3 max-w-2xl text-sm leading-7 text-[#c5e2dc]">
            Accede a las herramientas de gestión de la Evaluación de
            Ascenso de Docentes 2026.
          </p>

          <div class="mt-6 flex flex-wrap gap-3">
            <span
              class="inline-flex items-center gap-2 rounded-xl bg-white/10 px-4 py-2 text-sm"
            >
              <ShieldCheck :size="17" />
              {{ nombreRol }}
            </span>

            <span
              class="inline-flex items-center gap-2 rounded-xl bg-white/10 px-4 py-2 text-sm"
            >
              <CalendarDays :size="17" />
              Proceso 2026
            </span>
          </div>
        </div>
      </section>

      <!-- Módulos -->
      <section class="mt-10">
        <div class="mb-6">
          <h2 class="text-xl font-bold text-[#123f3b]">
            Módulos del sistema
          </h2>
          <p class="mt-1 text-sm text-slate-500">
            Selecciona el módulo que deseas administrar.
          </p>
        </div>

        <div class="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
          <RouterLink
            v-for="modulo in modulos"
            :key="modulo.ruta"
            :to="modulo.ruta"
            class="group flex flex-col rounded-2xl border border-[#dbe9e5] bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:border-[#9acdc1] hover:shadow-lg hover:shadow-[#17665e]/10"
          >
            <div
              class="mb-5 flex h-13 w-13 items-center justify-center rounded-2xl bg-[#e0f2ee] text-[#17665e] transition group-hover:bg-[#17665e] group-hover:text-white"
            >
              <component :is="modulo.icono" :size="25" />
            </div>

            <h3 class="text-lg font-bold text-[#123f3b]">
              {{ modulo.titulo }}
            </h3>

            <p class="mt-2 flex-1 text-sm leading-6 text-slate-500">
              {{ modulo.descripcion }}
            </p>

            <div
              class="mt-6 flex items-center justify-between border-t border-[#edf2f0] pt-4"
            >
              <span class="text-sm font-semibold text-[#17665e]">
                Abrir módulo
              </span>

              <ArrowRight
                :size="18"
                class="text-[#17665e] transition group-hover:translate-x-1"
              />
            </div>
          </RouterLink>
        </div>
      </section>

      <!-- Información -->
      <section
        class="mt-8 flex items-center gap-4 rounded-2xl border border-[#dbe9e5] bg-white p-5"
      >
        <div
          class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-[#e0f2ee] text-[#17665e]"
        >
          <ShieldCheck :size="22" />
        </div>

        <div>
          <p class="font-semibold text-[#123f3b]">
            Acceso autorizado
          </p>

          <p class="text-sm text-slate-500">
            Usuario: {{ authStore.usuario?.username }}
            · Rol: {{ authStore.rol }}
          </p>
        </div>
      </section>
    </div>
  </div>
</template>
