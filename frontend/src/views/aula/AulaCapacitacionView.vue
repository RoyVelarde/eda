<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import {
  DoorOpen,
  GraduationCap,
  Link2,
  CheckCircle2,
  Users,
  LoaderCircle,
  CalendarDays,
  Plus,
  X,
  ClipboardList,
} from 'lucide-vue-next'
import Swal from 'sweetalert2'

import { aulaService } from '@/services/aulaService'
import { capacitacionService } from '@/services/capacitacionService'
import { aulaCapacitacionService } from '@/services/aulaCapacitacionService'

import type { Aula, AulaCapacitacion } from '@/types/aula'
import type { Capacitacion } from '@/types/capacitacion'

const capacitaciones = ref<Capacitacion[]>([])
const aulas = ref<Aula[]>([])
const aulasAsignadas = ref<AulaCapacitacion[]>([])

const capacitacionSeleccionada = ref('')
const aulaSeleccionada = ref('')

const modalAbierto = ref(false)
const cargandoInicial = ref(true)
const cargandoAsignaciones = ref(false)
const asignando = ref(false)

const errorGeneral = ref('')

const idsAulasAsignadas = computed(() => new Set(aulasAsignadas.value.map((item) => item.aulaId)))

const aulasDisponibles = computed(() =>
  aulas.value.filter((aula) => aula.activa && !idsAulasAsignadas.value.has(aula.id)),
)

const aulaSeleccionadaActual = computed(() =>
  aulasDisponibles.value.find((aula) => aula.id === aulaSeleccionada.value),
)

const aulasAplicador = computed(
  () => aulasAsignadas.value.filter((aula) => aula.tipoAula.toUpperCase() === 'APLICADOR').length,
)

const aulasOrientador = computed(
  () => aulasAsignadas.value.filter((aula) => aula.tipoAula.toUpperCase() === 'ORIENTADOR').length,
)

const capacidadTotal = computed(() =>
  aulasAsignadas.value.reduce((total, aula) => total + (aula.capacidad ?? 0), 0),
)

const capacitacionActual = computed(() =>
  capacitaciones.value.find((capacitacion) => capacitacion.id === capacitacionSeleccionada.value),
)

function nombreCapacitacion(capacitacion: Capacitacion): string {
  const registro = capacitacion as Capacitacion & {
    nombre?: string
    titulo?: string
    anio?: number
  }

  return registro.nombre ?? registro.titulo ?? `Capacitación ${registro.anio ?? ''}`.trim()
}

function formatoFecha(fecha: string): string {
  if (!fecha) return '—'

  const valor = new Date(fecha)

  if (Number.isNaN(valor.getTime())) return fecha

  return valor.toLocaleString('es-PE', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

function etiquetaTipo(tipo: string): string {
  return tipo.toUpperCase() === 'APLICADOR' ? 'Aplicador' : 'Orientador'
}

function abrirModal(): void {
  if (!capacitacionSeleccionada.value) {
    void Swal.fire({
      icon: 'warning',
      title: 'Selecciona una capacitación',
      text: 'Primero debes elegir la capacitación a la que deseas asignar un aula.',
      confirmButtonColor: '#17665e',
    })
    return
  }

  if (aulasDisponibles.value.length === 0) {
    void Swal.fire({
      icon: 'info',
      title: 'No hay aulas disponibles',
      text: 'No existen aulas activas pendientes de asignación para esta capacitación.',
      confirmButtonColor: '#17665e',
    })
    return
  }

  aulaSeleccionada.value = ''
  modalAbierto.value = true
}

function cerrarModal(): void {
  if (asignando.value) return

  modalAbierto.value = false
  aulaSeleccionada.value = ''
}

function manejarTecla(event: KeyboardEvent): void {
  if (event.key === 'Escape' && modalAbierto.value) {
    cerrarModal()
  }
}

async function cargarDatosIniciales(): Promise<void> {
  cargandoInicial.value = true
  errorGeneral.value = ''

  try {
    const [listaCapacitaciones, listaAulas] = await Promise.all([
      capacitacionService.listar(),
      aulaService.listarActivas(),
    ])

    capacitaciones.value = listaCapacitaciones
    aulas.value = listaAulas.filter((aula) => aula.activa)

    if (
      capacitacionSeleccionada.value &&
      !capacitaciones.value.some((item) => item.id === capacitacionSeleccionada.value)
    ) {
      capacitacionSeleccionada.value = ''
    }
  } catch {
    errorGeneral.value =
      'No fue posible cargar las capacitaciones o las aulas. Verifica la conexión con el servidor.'
  } finally {
    cargandoInicial.value = false
  }
}

async function cargarAulasAsignadas(): Promise<void> {
  if (!capacitacionSeleccionada.value) {
    aulasAsignadas.value = []
    aulaSeleccionada.value = ''
    modalAbierto.value = false
    return
  }

  cargandoAsignaciones.value = true

  try {
    aulasAsignadas.value = await aulaCapacitacionService.listar(capacitacionSeleccionada.value)
  } catch {
    aulasAsignadas.value = []

    await Swal.fire({
      icon: 'error',
      title: 'No se pudieron consultar las asignaciones',
      text: 'Verifica que la capacitación exista y que tengas permisos para consultar sus aulas.',
      confirmButtonColor: '#17665e',
    })
  } finally {
    cargandoAsignaciones.value = false
  }
}

async function recargar(): Promise<void> {
  await cargarDatosIniciales()

  if (capacitacionSeleccionada.value) {
    await cargarAulasAsignadas()
  }
}

async function asignarAula(): Promise<void> {
  const capacitacionId = capacitacionSeleccionada.value
  const aulaId = aulaSeleccionada.value
  const aula = aulaSeleccionadaActual.value

  if (!capacitacionId) {
    await Swal.fire({
      icon: 'warning',
      title: 'Selecciona una capacitación',
      text: 'Debes elegir una capacitación antes de asignar un aula.',
      confirmButtonColor: '#17665e',
    })
    return
  }

  if (!aulaId || !aula) {
    await Swal.fire({
      icon: 'warning',
      title: 'Selecciona un aula',
      text: 'Elige una de las aulas disponibles.',
      confirmButtonColor: '#17665e',
    })
    return
  }

  if (idsAulasAsignadas.value.has(aulaId)) {
    await Swal.fire({
      icon: 'info',
      title: 'El aula ya está asignada',
      text: 'Actualiza la lista y selecciona otra aula disponible.',
      confirmButtonColor: '#17665e',
    })
    return
  }

  const resultado = await Swal.fire({
    icon: 'question',
    title: '¿Confirmar asignación?',
    html: `
      <div style="text-align:left">
        <p>Se vinculará el aula <strong>${aula.codigo}</strong> con la capacitación seleccionada.</p>
        <p style="margin-top:8px">Tipo: <strong>${etiquetaTipo(aula.tipo)}</strong></p>
        <p>Capacidad: <strong>${aula.capacidad} personas</strong></p>
      </div>
    `,
    showCancelButton: true,
    confirmButtonText: 'Sí, asignar',
    cancelButtonText: 'Volver',
    confirmButtonColor: '#17665e',
    cancelButtonColor: '#64748b',
  })

  if (!resultado.isConfirmed) return

  asignando.value = true

  try {
    await aulaCapacitacionService.asignar(capacitacionId, { aulaId })

    modalAbierto.value = false
    aulaSeleccionada.value = ''

    await cargarAulasAsignadas()

    await Swal.fire({
      icon: 'success',
      title: 'Aula asignada correctamente',
      text: `El aula ${aula.codigo} se vinculó con la capacitación.`,
      confirmButtonColor: '#17665e',
    })
  } catch (error: unknown) {
    const mensaje =
      typeof error === 'object' && error !== null && 'response' in error
        ? (() => {
            const respuesta = (
              error as {
                response?: {
                  data?: {
                    message?: string
                    detail?: string
                  }
                }
              }
            ).response

            return (
              respuesta?.data?.message ??
              respuesta?.data?.detail ??
              'No se pudo asignar el aula. Comprueba si ya está asignada o si existe algún conflicto.'
            )
          })()
        : 'No se pudo asignar el aula. Inténtalo nuevamente.'

    await Swal.fire({
      icon: 'error',
      title: 'Error al asignar el aula',
      text: mensaje,
      confirmButtonColor: '#17665e',
    })
  } finally {
    asignando.value = false
  }
}

watch(capacitacionSeleccionada, async () => {
  modalAbierto.value = false
  aulaSeleccionada.value = ''
  await cargarAulasAsignadas()
})

onMounted(() => {
  window.addEventListener('keydown', manejarTecla)
  void cargarDatosIniciales()
})

onUnmounted(() => {
  window.removeEventListener('keydown', manejarTecla)
})
</script>

<template>
  <main class="min-h-screen bg-[#edf5f3] p-4 md:p-6 xl:p-8">
    <div class="mx-auto w-full max-w-[1600px] space-y-6">
      <!-- Cabecera -->
      <header
        class="flex flex-col justify-between gap-4 rounded-2xl bg-[#17665e] px-6 py-6 text-white shadow-sm sm:flex-row sm:items-center sm:px-8"
      >
        <div>
          <div class="mb-2 flex items-center gap-2 text-sm font-semibold text-emerald-100">
            <Link2 class="h-4 w-4" />
            Gestión académica
          </div>

          <h1 class="text-2xl font-bold tracking-tight sm:text-3xl">Asignación de aulas</h1>

          <p class="mt-2 max-w-2xl text-sm text-emerald-50">
            Vincula las aulas creadas con una capacitación y consulta las asignaciones existentes.
          </p>
        </div>
      </header>

      <!-- Error de carga inicial -->
      <div
        v-if="errorGeneral"
        class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700"
      >
        {{ errorGeneral }}

        <button type="button" class="ml-2 font-bold underline" @click="recargar">Reintentar</button>
      </div>

      <!-- Carga inicial -->
      <div
        v-if="cargandoInicial"
        class="flex min-h-52 items-center justify-center rounded-2xl border border-[#dbe9e5] bg-white"
      >
        <div class="flex items-center gap-3 text-sm font-medium text-slate-600">
          <LoaderCircle class="h-5 w-5 animate-spin text-[#17665e]" />
          Cargando capacitaciones y aulas...
        </div>
      </div>

      <template v-else>
        <!-- Selector de capacitación -->
        <section class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm sm:p-6">
          <div class="mb-5 flex items-center gap-3">
            <div class="rounded-xl bg-[#e0f2ee] p-3">
              <GraduationCap class="h-6 w-6 text-[#17665e]" />
            </div>

            <div>
              <h2 class="font-bold">Seleccionar capacitación</h2>
              <p class="text-sm text-slate-500">Elige dónde deseas administrar las aulas.</p>
            </div>
          </div>

          <label for="capacitacion" class="mb-2 block text-sm font-semibold text-slate-700">
            Capacitación
          </label>

          <select
            id="capacitacion"
            v-model="capacitacionSeleccionada"
            class="w-full rounded-xl border border-[#dbe9e5] bg-white px-4 py-3 text-sm outline-none transition focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/15 sm:max-w-2xl"
          >
            <option value="">Selecciona una capacitación</option>

            <option
              v-for="capacitacion in capacitaciones"
              :key="capacitacion.id"
              :value="capacitacion.id"
            >
              {{ nombreCapacitacion(capacitacion) }}
            </option>
          </select>

          <p v-if="capacitaciones.length === 0" class="mt-3 text-sm text-amber-700">
            No hay capacitaciones registradas.
          </p>

          <div v-if="capacitacionActual" class="mt-4 rounded-xl bg-[#f1f8f6] p-4 text-sm">
            <span class="font-semibold">Capacitación seleccionada:</span>
            {{ nombreCapacitacion(capacitacionActual) }}
          </div>
        </section>

        <!-- Resumen -->
        <section class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-slate-500"> Aulas asignadas </span>
              <div class="rounded-lg bg-[#e0f2ee] p-2">
                <DoorOpen class="h-5 w-5 text-[#17665e]" />
              </div>
            </div>

            <p class="mt-3 text-3xl font-bold">{{ aulasAsignadas.length }}</p>
            <p class="mt-1 text-xs text-slate-500">Aulas vinculadas a la capacitación.</p>
          </article>

          <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-slate-500"> Aulas aplicador </span>
              <div class="rounded-lg bg-blue-50 p-2">
                <Users class="h-5 w-5 text-blue-700" />
              </div>
            </div>

            <p class="mt-3 text-3xl font-bold">{{ aulasAplicador }}</p>
            <p class="mt-1 text-xs text-slate-500">Aulas de tipo aplicador.</p>
          </article>

          <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-slate-500"> Aulas orientador </span>
              <div class="rounded-lg bg-amber-50 p-2">
                <Users class="h-5 w-5 text-amber-700" />
              </div>
            </div>

            <p class="mt-3 text-3xl font-bold">{{ aulasOrientador }}</p>
            <p class="mt-1 text-xs text-slate-500">Aulas de tipo orientador.</p>
          </article>

          <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-slate-500"> Capacidad total </span>
              <div class="rounded-lg bg-violet-50 p-2">
                <ClipboardList class="h-5 w-5 text-violet-700" />
              </div>
            </div>

            <p class="mt-3 text-3xl font-bold">{{ capacidadTotal }}</p>
            <p class="mt-1 text-xs text-slate-500">Plazas disponibles según aforo.</p>
          </article>
        </section>

        <!-- Acciones de asignación -->
        <section
          class="flex flex-col justify-between gap-4 rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm sm:flex-row sm:items-center sm:p-6"
        >
          <div>
            <h2 class="font-bold">Administrar aulas</h2>
            <p class="mt-1 text-sm text-slate-500">
              Asigna aulas activas que todavía no estén vinculadas.
            </p>
          </div>

          <button
            type="button"
            :disabled="!capacitacionSeleccionada || cargandoAsignaciones"
            class="inline-flex items-center justify-center gap-2 rounded-xl bg-[#17665e] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#12554e] disabled:cursor-not-allowed disabled:opacity-50"
            @click="abrirModal"
          >
            <Plus class="h-5 w-5" />
            Asignar aula
          </button>
        </section>

        <!-- Tabla -->
        <section class="overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white shadow-sm">
          <div
            class="flex flex-col gap-2 border-b border-[#dbe9e5] p-5 sm:flex-row sm:items-center sm:justify-between sm:p-6"
          >
            <div>
              <h2 class="font-bold">Aulas vinculadas</h2>
              <p class="mt-1 text-sm text-slate-500">
                Consulta las aulas asociadas a la capacitación seleccionada.
              </p>
            </div>

            <span
              class="w-fit rounded-full bg-[#e0f2ee] px-3 py-1 text-xs font-bold text-[#17665e]"
            >
              {{ aulasAsignadas.length }}
              {{ aulasAsignadas.length === 1 ? 'aula' : 'aulas' }}
            </span>
          </div>

          <div v-if="!capacitacionSeleccionada" class="p-10 text-center text-sm text-slate-500">
            Selecciona una capacitación para consultar sus aulas.
          </div>

          <div
            v-else-if="cargandoAsignaciones"
            class="flex items-center justify-center gap-3 p-10 text-sm text-slate-500"
          >
            <LoaderCircle class="h-5 w-5 animate-spin text-[#17665e]" />
            Consultando aulas asignadas...
          </div>

          <div v-else-if="aulasAsignadas.length === 0" class="p-10 text-center">
            <div
              class="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-[#e0f2ee]"
            >
              <DoorOpen class="h-6 w-6 text-[#17665e]" />
            </div>

            <p class="font-semibold">Todavía no hay aulas asignadas</p>
            <p class="mt-1 text-sm text-slate-500">Usa el botón «Asignar aula» para comenzar.</p>
          </div>

          <div v-else class="overflow-x-auto">
            <table class="min-w-full divide-y divide-[#e5efec] text-sm">
              <thead class="bg-[#f7fbfa] text-left text-xs uppercase tracking-wide text-slate-500">
                <tr>
                  <th class="px-5 py-4 font-semibold">Código</th>
                  <th class="px-5 py-4 font-semibold">Tipo de aula</th>
                  <th class="px-5 py-4 font-semibold">Capacidad</th>
                  <th class="px-5 py-4 font-semibold">Fecha de asignación</th>
                  <th class="px-5 py-4 font-semibold">Estado</th>
                </tr>
              </thead>

              <tbody class="divide-y divide-[#e5efec]">
                <tr
                  v-for="aula in aulasAsignadas"
                  :key="aula.id"
                  class="transition hover:bg-[#f7fbfa]"
                >
                  <td class="whitespace-nowrap px-5 py-4 font-semibold text-[#17665e]">
                    {{ aula.codigoAula }}
                  </td>

                  <td class="whitespace-nowrap px-5 py-4">
                    <span
                      class="inline-flex rounded-full px-3 py-1 text-xs font-semibold"
                      :class="
                        aula.tipoAula.toUpperCase() === 'APLICADOR'
                          ? 'bg-blue-50 text-blue-700'
                          : 'bg-amber-50 text-amber-700'
                      "
                    >
                      {{ etiquetaTipo(aula.tipoAula) }}
                    </span>
                  </td>

                  <td class="whitespace-nowrap px-5 py-4">
                    <span class="inline-flex items-center gap-2">
                      <Users class="h-4 w-4 text-slate-400" />
                      {{ aula.capacidad }} personas
                    </span>
                  </td>

                  <td class="whitespace-nowrap px-5 py-4 text-slate-600">
                    <span class="inline-flex items-center gap-2">
                      <CalendarDays class="h-4 w-4 text-slate-400" />
                      {{ formatoFecha(aula.fechaAsignacion) }}
                    </span>
                  </td>

                  <td class="whitespace-nowrap px-5 py-4">
                    <span
                      class="inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold"
                      :class="
                        aula.activa
                          ? 'bg-emerald-50 text-emerald-700'
                          : 'bg-slate-100 text-slate-600'
                      "
                    >
                      <CheckCircle2 class="h-3.5 w-3.5" />
                      {{ aula.activa ? 'Activa' : 'Inactiva' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </template>
    </div>

    <!-- Modal de asignación -->
    <Teleport to="body">
      <Transition
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition duration-150 ease-in"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div
          v-if="modalAbierto"
          class="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-slate-950/50 p-4 backdrop-blur-sm"
          @click.self="cerrarModal"
        >
          <section
            role="dialog"
            aria-modal="true"
            aria-labelledby="modal-titulo"
            class="my-auto w-full max-w-xl overflow-hidden rounded-2xl bg-white shadow-2xl"
          >
            <!-- Cabecera del modal -->
            <header
              class="flex items-start justify-between gap-4 bg-[#17665e] px-6 py-5 text-white"
            >
              <div class="flex items-center gap-3">
                <div class="rounded-xl bg-white/15 p-3">
                  <DoorOpen class="h-6 w-6" />
                </div>

                <div>
                  <h2 id="modal-titulo" class="text-lg font-bold">Asignar aula</h2>
                  <p class="mt-1 text-sm text-emerald-50">
                    Vincular un aula a la capacitación seleccionada.
                  </p>
                </div>
              </div>

              <button
                type="button"
                :disabled="asignando"
                aria-label="Cerrar modal"
                class="rounded-lg p-2 text-white transition hover:bg-white/15 disabled:opacity-50"
                @click="cerrarModal"
              >
                <X class="h-5 w-5" />
              </button>
            </header>

            <!-- Formulario -->
            <form class="space-y-5 p-6" @submit.prevent="asignarAula">
              <div>
                <label class="mb-2 block text-sm font-semibold text-slate-700">
                  Capacitación
                </label>

                <div
                  class="rounded-xl border border-[#dbe9e5] bg-[#f7fbfa] px-4 py-3 text-sm text-slate-700"
                >
                  {{
                    capacitacionActual ? nombreCapacitacion(capacitacionActual) : 'Sin seleccionar'
                  }}
                </div>
              </div>

              <div>
                <label for="aula" class="mb-2 block text-sm font-semibold text-slate-700">
                  Aula disponible <span class="text-red-500">*</span>
                </label>

                <select
                  id="aula"
                  v-model="aulaSeleccionada"
                  required
                  :disabled="asignando || aulasDisponibles.length === 0"
                  class="w-full rounded-xl border border-[#dbe9e5] bg-white px-4 py-3 text-sm outline-none transition focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/15 disabled:bg-slate-50"
                >
                  <option value="" disabled>Selecciona un aula</option>

                  <option v-for="aula in aulasDisponibles" :key="aula.id" :value="aula.id">
                    {{ aula.codigo }} — {{ etiquetaTipo(aula.tipo) }}
                  </option>
                </select>

                <p class="mt-2 text-xs text-slate-500">
                  Las aulas ya asignadas a esta capacitación no aparecen en esta lista.
                </p>
              </div>

              <!-- Datos del aula seleccionada -->
              <div
                v-if="aulaSeleccionadaActual"
                class="rounded-xl border border-[#cde5dd] bg-[#f1f8f6] p-4"
              >
                <p class="mb-3 text-xs font-bold uppercase tracking-wide text-[#17665e]">
                  Detalles del aula
                </p>

                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <p class="text-xs text-slate-500">Código</p>
                    <p class="mt-1 font-bold">
                      {{ aulaSeleccionadaActual.codigo }}
                    </p>
                  </div>

                  <div>
                    <p class="text-xs text-slate-500">Tipo</p>
                    <p class="mt-1 font-bold">
                      {{ etiquetaTipo(aulaSeleccionadaActual.tipo) }}
                    </p>
                  </div>

                  <div class="col-span-2">
                    <p class="text-xs text-slate-500">Capacidad máxima</p>
                    <p class="mt-1 flex items-center gap-2 font-bold">
                      <Users class="h-4 w-4 text-[#17665e]" />
                      {{ aulaSeleccionadaActual.capacidad }} personas
                    </p>
                  </div>
                </div>
              </div>

              <div
                v-if="aulasDisponibles.length === 0"
                class="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800"
              >
                No hay aulas disponibles para asignar a esta capacitación.
              </div>

              <!-- Botones -->
              <footer
                class="flex flex-col-reverse gap-3 border-t border-slate-100 pt-5 sm:flex-row sm:justify-end"
              >
                <button
                  type="button"
                  :disabled="asignando"
                  class="inline-flex items-center justify-center rounded-xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 disabled:opacity-50"
                  @click="cerrarModal"
                >
                  Cancelar
                </button>

                <button
                  type="submit"
                  :disabled="!aulaSeleccionada || asignando || cargandoAsignaciones"
                  class="inline-flex items-center justify-center gap-2 rounded-xl bg-[#17665e] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#12554e] disabled:cursor-not-allowed disabled:opacity-50"
                >
                  <LoaderCircle v-if="asignando" class="h-4 w-4 animate-spin" />
                  <Link2 v-else class="h-4 w-4" />
                  {{ asignando ? 'Asignando...' : 'Confirmar asignación' }}
                </button>
              </footer>
            </form>
          </section>
        </div>
      </Transition>
    </Teleport>
  </main>
</template>
