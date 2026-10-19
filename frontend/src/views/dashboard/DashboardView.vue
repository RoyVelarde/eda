<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  ArrowRight,
  BookOpen,
  CalendarDays,
  ClipboardList,
  LayoutDashboard,
  MapPin,
  ShieldCheck,
  UsersRound,
  GraduationCap,
  Sparkles,
  ChevronRight,
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

const nombreUsuario = computed(() => {
  return authStore.usuario?.nombres?.trim() || 'Usuario'
})

const modulos = [
  {
    titulo: 'Personal',
    descripcion: 'Consulta y administra los registros del personal de nivel 2 y nivel 3.',
    detalle: 'Gestión de personal',
    icono: UsersRound,
    ruta: '/personal',
    numero: '01',
  },
  {
    titulo: 'Capacitaciones',
    descripcion: 'Gestiona los procesos de capacitación y consulta su información.',
    detalle: 'Gestión de procesos',
    icono: BookOpen,
    ruta: '/capacitaciones',
    numero: '02',
  },
  {
    titulo: 'Participantes',
    descripcion: 'Consulta a los participantes y la información de sus asignaciones.',
    detalle: 'Consulta de participantes',
    icono: ClipboardList,
    ruta: '/participantes',
    numero: '03',
  },
]
</script>

<template>
  <main class="min-h-screen bg-[#edf5f3]">
    <div class="mx-auto w-full max-w-[1600px] px-4 py-6 sm:px-6 lg:px-8 xl:px-10">
      <!-- Cabecera institucional -->
      <header
        class="relative mb-7 overflow-hidden rounded-3xl bg-linear-to-r from-[#123f3b] via-[#17665e] to-[#218477] px-6 py-7 text-white shadow-lg shadow-[#123f3b]/10 sm:px-9 sm:py-8 lg:px-10"
      >
        <!-- Decoración -->
        <div
          class="pointer-events-none absolute -right-16 -top-28 h-80 w-80 rounded-full border border-white/10"
        />
        <div
          class="pointer-events-none absolute -right-2 -top-16 h-64 w-64 rounded-full border border-white/10"
        />
        <div
          class="pointer-events-none absolute -bottom-24 right-1/4 h-52 w-52 rounded-full bg-white/4"
        />

        <div class="relative z-10 flex flex-col justify-between gap-7 lg:flex-row lg:items-center">
          <div>
            <div
              class="mb-5 inline-flex items-center gap-2 rounded-full border border-white/20 bg-white/10 px-3.5 py-2 text-xs font-semibold tracking-wide text-emerald-50"
            >
              <LayoutDashboard :size="15" />
              PANEL DE ADMINISTRACIÓN
            </div>

            <div class="flex items-start gap-4">
              <div
                class="hidden h-16 w-16 shrink-0 items-center justify-center rounded-2xl border border-white/20 bg-white/10 shadow-inner sm:flex"
              >
                <GraduationCap :size="34" :stroke-width="1.7" />
              </div>

              <div>
                <p class="text-sm font-medium text-emerald-100">
                  Evaluación de Ascenso de Docentes
                </p>

                <h1 class="mt-1 text-3xl font-extrabold tracking-tight sm:text-4xl">EDA 2026</h1>

                <p class="mt-3 max-w-2xl text-sm leading-6 text-emerald-50/85 sm:text-base">
                  Plataforma de gestión y seguimiento del proceso de evaluación para la sede
                  Ayacucho - Huamanga.
                </p>
              </div>
            </div>
          </div>

          <div
            class="flex shrink-0 flex-col gap-3 rounded-2xl border border-white/15 bg-black/10 p-4 backdrop-blur-sm sm:min-w-64"
          >
            <div class="flex items-center gap-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-white/15">
                <MapPin :size="20" />
              </div>

              <div>
                <p class="text-xs text-emerald-100/80">Sede operativa</p>
                <p class="mt-0.5 text-sm font-bold">Ayacucho - Huamanga</p>
              </div>
            </div>

            <div class="h-px bg-white/15" />

            <div class="flex items-center gap-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-white/15">
                <CalendarDays :size="20" />
              </div>

              <div>
                <p class="text-xs text-emerald-100/80">Proceso de evaluación</p>
                <p class="mt-0.5 text-sm font-bold">Periodo 2026</p>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- Bienvenida -->
      <section
        class="mb-9 flex flex-col justify-between gap-5 rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm sm:flex-row sm:items-center sm:p-6 lg:px-8"
      >
        <div class="flex items-start gap-4">
          <div
            class="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-[#e0f2ee] text-[#17665e]"
          >
            <Sparkles :size="26" />
          </div>

          <div>
            <p class="text-sm font-medium text-slate-500">Bienvenido al sistema</p>

            <h2 class="mt-1 text-xl font-bold text-[#123f3b] sm:text-2xl">
              ¡Hola, {{ nombreUsuario }}!
            </h2>

            <p class="mt-2 max-w-2xl text-sm leading-6 text-slate-500">
              Desde este panel puedes acceder a los módulos disponibles para gestionar tus
              actividades del proceso EDA 2026.
            </p>
          </div>
        </div>

        <div
          class="flex shrink-0 items-center gap-3 self-start rounded-xl border border-[#dbe9e5] bg-[#f5faf8] px-4 py-3 sm:self-center"
        >
          <div
            class="flex h-10 w-10 items-center justify-center rounded-xl bg-[#17665e] text-white"
          >
            <ShieldCheck :size="21" />
          </div>

          <div>
            <p class="text-[11px] font-semibold uppercase tracking-wider text-slate-400">
              Rol de usuario
            </p>
            <p class="mt-1 text-sm font-bold text-[#123f3b]">
              {{ nombreRol }}
            </p>
          </div>
        </div>
      </section>

      <!-- Encabezado de módulos -->
      <section>
        <div class="mb-5 flex flex-col justify-between gap-3 sm:flex-row sm:items-end">
          <div>
            <div class="flex items-center gap-2 text-sm font-semibold text-[#17665e]">
              <LayoutDashboard :size="17" />
              Espacio de trabajo
            </div>

            <h2 class="mt-2 text-2xl font-extrabold tracking-tight text-[#123f3b]">
              Módulos del sistema
            </h2>

            <p class="mt-2 text-sm text-slate-500">
              Selecciona una opción para ingresar a sus funcionalidades.
            </p>
          </div>

          <div
            class="inline-flex w-fit items-center gap-2 rounded-full border border-[#dbe9e5] bg-white px-3.5 py-2 text-xs font-medium text-slate-500"
          >
            <span class="h-2 w-2 rounded-full bg-emerald-500" />
            Plataforma disponible
          </div>
        </div>

        <!-- Tarjetas -->
        <div class="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-3">
          <RouterLink
            v-for="modulo in modulos"
            :key="modulo.ruta"
            :to="modulo.ruta"
            class="group relative flex min-h-64 flex-col overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm transition duration-200 hover:-translate-y-1 hover:border-[#9acdc1] hover:shadow-xl hover:shadow-[#17665e]/10 focus:outline-none focus-visible:ring-2 focus-visible:ring-[#17665e] focus-visible:ring-offset-2 sm:p-6"
          >
            <!-- Línea decorativa -->
            <div
              class="absolute inset-x-0 top-0 h-1 origin-left scale-x-0 bg-linear-to-r from-[#17665e] to-[#52b5a3] transition-transform duration-300 group-hover:scale-x-100"
            />

            <div class="flex items-start justify-between gap-3">
              <div
                class="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#e0f2ee] text-[#17665e] transition duration-200 group-hover:bg-[#17665e] group-hover:text-white"
              >
                <component :is="modulo.icono" :size="27" :stroke-width="1.8" />
              </div>

              <span
                class="rounded-lg bg-[#f2f7f5] px-2.5 py-1 text-xs font-bold tracking-wider text-[#7b9992]"
              >
                {{ modulo.numero }}
              </span>
            </div>

            <div class="mt-5">
              <h3
                class="text-lg font-bold text-[#123f3b] transition-colors group-hover:text-[#17665e]"
              >
                {{ modulo.titulo }}
              </h3>

              <p class="mt-2 text-xs font-semibold uppercase tracking-wide text-[#57978b]">
                {{ modulo.detalle }}
              </p>

              <p class="mt-3 text-sm leading-6 text-slate-500">
                {{ modulo.descripcion }}
              </p>
            </div>

            <div class="mt-auto flex items-center justify-between border-t border-[#edf2f0] pt-4">
              <span class="text-sm font-semibold text-[#17665e]"> Ingresar al módulo </span>

              <span
                class="flex h-9 w-9 items-center justify-center rounded-xl bg-[#f0f7f5] text-[#17665e] transition duration-200 group-hover:translate-x-1 group-hover:bg-[#17665e] group-hover:text-white"
              >
                <ArrowRight :size="18" />
              </span>
            </div>
          </RouterLink>
        </div>
      </section>

      <!-- Información de sesión -->
      <section class="mt-9 overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white shadow-sm">
        <div class="flex flex-col gap-4 p-5 sm:flex-row sm:items-center sm:justify-between sm:px-6">
          <div class="flex items-center gap-3">
            <div
              class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-[#e0f2ee] text-[#17665e]"
            >
              <ShieldCheck :size="22" />
            </div>

            <div>
              <p class="font-bold text-[#123f3b]">Sesión autenticada</p>

              <p class="mt-1 text-xs leading-5 text-slate-500 sm:text-sm">
                Has ingresado al sistema con tus credenciales.
              </p>
            </div>
          </div>

          <div class="flex flex-wrap gap-2 sm:justify-end">
            <span
              class="inline-flex items-center gap-2 rounded-lg border border-[#dbe9e5] bg-[#f7faf9] px-3 py-2 text-xs font-medium text-slate-600"
            >
              <UsersRound :size="15" class="text-[#17665e]" />
              {{ authStore.usuario?.username ?? 'Usuario autenticado' }}
            </span>

            <span
              class="inline-flex items-center gap-2 rounded-lg border border-[#cde7df] bg-[#eaf7f2] px-3 py-2 text-xs font-semibold text-[#17665e]"
            >
              <ShieldCheck :size="15" />
              {{ authStore.rol ?? 'Usuario' }}
            </span>
          </div>
        </div>

        <div
          class="flex flex-col gap-2 border-t border-[#edf2f0] bg-[#f8fbfa] px-5 py-3 text-xs text-slate-500 sm:flex-row sm:items-center sm:justify-between sm:px-6"
        >
          <span>EDA 2026 · Sede Ayacucho - Huamanga</span>
          <span class="inline-flex items-center gap-1.5">
            <ShieldCheck :size="14" class="text-[#17665e]" />
            Acceso protegido
            <ChevronRight :size="14" class="text-slate-400" />
          </span>
        </div>
      </section>

      <!-- Pie -->
      <footer class="py-6 text-center text-xs text-slate-400">
        Sistema EDA 2026 · Evaluación de Ascenso de Docentes
      </footer>
    </div>
  </main>
</template>
