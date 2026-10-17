<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft,
  BriefcaseBusiness,
  CalendarDays,
  ClipboardList,
  Contact,
  FileUser,
  IdCard,
  Mail,
  Phone,
  RefreshCw,
  UserRound,
  UsersRound,
} from 'lucide-vue-next'

import { personalService } from '@/services/personalService'
import type { Personal } from '@/types/personal'

const route = useRoute()
const router = useRouter()

const personal = ref<Personal | null>(null)
const cargando = ref(false)
const error = ref('')

const nombreCompleto = computed(() => {
  if (!personal.value) return ''

  return [personal.value.apellidoPaterno, personal.value.apellidoMaterno, personal.value.nombres]
    .filter(Boolean)
    .join(' ')
})

const nivelTexto = computed(() => {
  return personal.value?.nivel === 'NIVEL_2' ? 'Nivel 2' : 'Nivel 3'
})

const fechaCreacion = computed(() => {
  const fecha = personal.value?.fechaCreacion

  if (!fecha) return 'No disponible'

  const fechaValida = new Date(fecha)

  if (Number.isNaN(fechaValida.getTime())) {
    return fecha
  }

  return new Intl.DateTimeFormat('es-PE', {
    dateStyle: 'long',
    timeZone: 'America/Lima',
  }).format(fechaValida)
})

async function cargarDetalle() {
  cargando.value = true
  error.value = ''
  personal.value = null

  try {
    const id = String(route.params.id ?? '')

    if (!id.trim()) {
      throw new Error('No se proporcionó el identificador del personal.')
    }

    personal.value = await personalService.obtenerPorId(id)
  } catch (e: unknown) {
    const errorApi = e as {
      response?: {
        status?: number
        data?: {
          mensaje?: string
          message?: string
          error?: string
        }
      }
    }

    error.value =
      errorApi.response?.data?.mensaje ??
      errorApi.response?.data?.message ??
      (errorApi.response?.status === 404
        ? 'No se encontró el registro solicitado.'
        : errorApi.response?.status === 401 || errorApi.response?.status === 403
          ? 'No tienes autorización para consultar este registro.'
          : 'No se pudo cargar la información. Comprueba la conexión e inténtalo nuevamente.')
  } finally {
    cargando.value = false
  }
}

function volverAlListado() {
  void router.push({ name: 'personal' })
}

onMounted(() => {
  void cargarDetalle()
})
</script>

<template>
  <div class="min-h-screen bg-[#edf5f3] px-5 py-8 sm:px-8">
    <div class="mx-auto max-w-5xl">
      <!-- Encabezado -->
      <header class="mb-8">
        <button
          type="button"
          class="mb-5 inline-flex items-center gap-2 rounded-lg text-sm font-semibold text-[#17665e] transition hover:text-[#123f3b]"
          @click="volverAlListado"
        >
          <ArrowLeft :size="18" />
          Volver al listado
        </button>

        <div class="flex items-center gap-2 text-sm text-[#17665e]">
          <UsersRound :size="18" />
          Gestión del personal
        </div>

        <h1 class="mt-2 text-2xl font-bold text-[#123f3b] sm:text-3xl">Detalle del personal</h1>

        <p class="mt-2 text-sm text-slate-500">
          Consulta la información completa del registro seleccionado.
        </p>
      </header>

      <!-- Cargando -->
      <section
        v-if="cargando"
        class="rounded-2xl border border-[#dbe9e5] bg-white px-6 py-16 text-center shadow-sm"
      >
        <RefreshCw :size="34" class="mx-auto animate-spin text-[#17665e]" />

        <p class="mt-4 font-semibold text-[#123f3b]">Cargando información...</p>

        <p class="mt-1 text-sm text-slate-500">Consultando los datos del personal.</p>
      </section>

      <!-- Error -->
      <section
        v-else-if="error"
        role="alert"
        class="rounded-2xl border border-red-200 bg-white p-6 shadow-sm sm:p-8"
      >
        <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-red-50 text-red-600">
          <Contact :size="25" />
        </div>

        <h2 class="mt-4 text-lg font-bold text-[#123f3b]">No se pudo cargar el registro</h2>

        <p class="mt-2 text-sm leading-6 text-slate-500">
          {{ error }}
        </p>

        <div class="mt-6 flex flex-wrap gap-3">
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-xl bg-[#17665e] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#123f3b]"
            @click="cargarDetalle"
          >
            <RefreshCw :size="17" />
            Intentar nuevamente
          </button>

          <button
            type="button"
            class="rounded-xl border border-[#cfe0db] bg-white px-5 py-3 text-sm font-semibold text-[#17665e] transition hover:bg-[#f5faf8]"
            @click="volverAlListado"
          >
            Volver al listado
          </button>
        </div>
      </section>

      <!-- Información -->
      <template v-else-if="personal">
        <!-- Resumen -->
        <section
          class="mb-6 overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white shadow-sm"
        >
          <div class="bg-[#17665e] px-6 py-7 sm:px-8">
            <div class="flex flex-col gap-5 sm:flex-row sm:items-center">
              <div
                class="flex h-16 w-16 shrink-0 items-center justify-center rounded-2xl bg-white/15 text-white ring-1 ring-white/25"
              >
                <UserRound :size="34" />
              </div>

              <div class="min-w-0 flex-1">
                <p class="text-sm font-medium text-emerald-100">Ficha de personal</p>

                <h2 class="mt-1 wrap-break-word text-2xl font-bold text-white">
                  {{ nombreCompleto }}
                </h2>

                <p class="mt-2 text-sm text-emerald-50">
                  {{ personal.cargo }}
                </p>

                <div class="mt-4 flex flex-wrap gap-2">
                  <span
                    class="inline-flex items-center rounded-lg bg-white/15 px-3 py-1.5 text-xs font-semibold text-white"
                  >
                    {{ nivelTexto }}
                  </span>

                  <span
                    v-if="personal.nivel === 'NIVEL_3' && personal.situacion"
                    class="inline-flex items-center rounded-lg px-3 py-1.5 text-xs font-semibold"
                    :class="
                      personal.situacion.toUpperCase() === 'TITULAR'
                        ? 'bg-emerald-100 text-emerald-800'
                        : 'bg-amber-100 text-amber-800'
                    "
                  >
                    {{ personal.situacion }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="grid gap-4 p-5 sm:grid-cols-3 sm:p-6">
            <div class="rounded-xl border border-[#dbe9e5] bg-[#f5faf8] p-4">
              <div class="flex items-center gap-2 text-[#17665e]">
                <IdCard :size="18" />
                <span class="text-xs font-semibold uppercase tracking-wide"> DNI </span>
              </div>

              <p class="mt-3 text-lg font-bold text-[#123f3b]">
                {{ personal.dni }}
              </p>
            </div>

            <div class="rounded-xl border border-[#dbe9e5] bg-[#f5faf8] p-4">
              <div class="flex items-center gap-2 text-[#17665e]">
                <BriefcaseBusiness :size="18" />
                <span class="text-xs font-semibold uppercase tracking-wide"> Cargo </span>
              </div>

              <p class="mt-3 font-bold text-[#123f3b]">
                {{ personal.cargo }}
              </p>
            </div>

            <div class="rounded-xl border border-[#dbe9e5] bg-[#f5faf8] p-4">
              <div class="flex items-center gap-2 text-[#17665e]">
                <ClipboardList :size="18" />
                <span class="text-xs font-semibold uppercase tracking-wide"> Nivel </span>
              </div>

              <p class="mt-3 text-lg font-bold text-[#123f3b]">
                {{ nivelTexto }}
              </p>
            </div>
          </div>
        </section>

        <!-- Datos de contacto -->
        <section
          class="mb-6 overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white shadow-sm"
        >
          <div class="border-b border-[#e7efec] px-6 py-5 sm:px-8">
            <div class="flex items-center gap-3">
              <div class="rounded-xl bg-[#e0f2ee] p-2.5 text-[#17665e]">
                <Contact :size="21" />
              </div>

              <div>
                <h3 class="font-bold text-[#123f3b]">Información de contacto</h3>

                <p class="mt-1 text-sm text-slate-500">
                  Datos personales registrados en el sistema.
                </p>
              </div>
            </div>
          </div>

          <div class="grid gap-6 p-6 sm:grid-cols-2 sm:p-8">
            <div class="flex items-start gap-3">
              <div class="rounded-lg bg-[#e0f2ee] p-2 text-[#17665e]">
                <Mail :size="19" />
              </div>

              <div class="min-w-0">
                <p class="text-xs font-medium uppercase tracking-wide text-slate-500">
                  Correo electrónico
                </p>

                <p class="mt-2 break-all font-semibold text-[#123f3b]">
                  {{ personal.correo || 'Sin correo registrado' }}
                </p>
              </div>
            </div>

            <div class="flex items-start gap-3">
              <div class="rounded-lg bg-[#e0f2ee] p-2 text-[#17665e]">
                <Phone :size="19" />
              </div>

              <div>
                <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Celular</p>

                <p class="mt-2 font-semibold text-[#123f3b]">
                  {{ personal.celular || 'Sin celular registrado' }}
                </p>
              </div>
            </div>
          </div>
        </section>

        <!-- Información complementaria -->
        <section class="overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white shadow-sm">
          <div class="border-b border-[#e7efec] px-6 py-5 sm:px-8">
            <div class="flex items-center gap-3">
              <div class="rounded-xl bg-[#e0f2ee] p-2.5 text-[#17665e]">
                <FileUser :size="21" />
              </div>

              <div>
                <h3 class="font-bold text-[#123f3b]">Información complementaria</h3>

                <p class="mt-1 text-sm text-slate-500">
                  Datos adicionales según el nivel de personal.
                </p>
              </div>
            </div>
          </div>

          <div class="grid gap-6 p-6 sm:grid-cols-2 sm:p-8">
            <div v-if="personal.nivel === 'NIVEL_3'" class="flex items-start gap-3">
              <div class="rounded-lg bg-[#e0f2ee] p-2 text-[#17665e]">
                <ClipboardList :size="19" />
              </div>

              <div>
                <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Ranking</p>

                <p class="mt-2 font-semibold text-[#123f3b]">
                  {{ personal.ranking ?? 'No registrado' }}
                </p>
              </div>
            </div>

            <div v-if="personal.nivel === 'NIVEL_3'" class="flex items-start gap-3">
              <div class="rounded-lg bg-[#e0f2ee] p-2 text-[#17665e]">
                <UsersRound :size="19" />
              </div>

              <div>
                <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Situación</p>

                <p class="mt-2 font-semibold text-[#123f3b]">
                  {{ personal.situacion || 'No registrada' }}
                </p>
              </div>
            </div>

            <div class="flex items-start gap-3">
              <div class="rounded-lg bg-[#e0f2ee] p-2 text-[#17665e]">
                <CalendarDays :size="19" />
              </div>

              <div>
                <p class="text-xs font-medium uppercase tracking-wide text-slate-500">
                  Fecha de registro
                </p>

                <p class="mt-2 font-semibold text-[#123f3b]">
                  {{ fechaCreacion }}
                </p>
              </div>
            </div>
          </div>

          <footer
            class="flex flex-col-reverse gap-3 border-t border-[#e7efec] bg-[#fbfdfc] px-6 py-5 sm:flex-row sm:justify-end sm:px-8"
          >
            <button
              type="button"
              class="inline-flex items-center justify-center gap-2 rounded-xl border border-[#cfe0db] bg-white px-5 py-3 text-sm font-semibold text-[#17665e] transition hover:bg-[#f5faf8]"
              @click="volverAlListado"
            >
              <ArrowLeft :size="17" />
              Volver al listado
            </button>
          </footer>
        </section>
      </template>
    </div>
  </div>
</template>
