<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  BookOpen,
  CalendarDays,
  CheckCircle2,
  CircleAlert,
  Clock3,
  LoaderCircle,
  Plus,
  RefreshCw,
  Search,
  X,
} from 'lucide-vue-next'

import Swal from 'sweetalert2'
import { capacitacionService } from '@/services/capacitacionService'
import type { Capacitacion, CapacitacionRequest, EstadoCapacitacion } from '@/types/capacitacion'

const capacitaciones = ref<Capacitacion[]>([])
const cargando = ref(false)
const guardando = ref(false)
const iniciandoId = ref<string | null>(null)

const error = ref('')
const mensaje = ref('')
const busqueda = ref('')
const filtroAnio = ref('')
const filtroEstado = ref<EstadoCapacitacion | ''>('')

const modalAbierto = ref(false)

const formulario = ref<CapacitacionRequest>({
  nombre: '',
  anio: new Date().getFullYear(),
  fechaInicio: '',
  fechaFin: '',
})

const erroresFormulario = ref<Record<string, string>>({})

const estados: { valor: EstadoCapacitacion; etiqueta: string }[] = [
  { valor: 'PLANIFICADA', etiqueta: 'Planificada' },
  { valor: 'EN_CURSO', etiqueta: 'En curso' },
  { valor: 'FINALIZADA', etiqueta: 'Finalizada' },
  { valor: 'CANCELADA', etiqueta: 'Cancelada' },
]

const aniosDisponibles = computed(() => {
  const anios = new Set(capacitaciones.value.map((capacitacion) => capacitacion.anio))

  anios.add(new Date().getFullYear())

  return [...anios].sort((a, b) => b - a)
})

const capacitacionesFiltradas = computed(() => {
  const texto = busqueda.value.trim().toLocaleLowerCase()

  return capacitaciones.value.filter((capacitacion) => {
    const coincideBusqueda =
      !texto ||
      capacitacion.nombre.toLocaleLowerCase().includes(texto) ||
      String(capacitacion.anio).includes(texto)

    const coincideAnio = !filtroAnio.value || capacitacion.anio === Number(filtroAnio.value)

    const coincideEstado = !filtroEstado.value || capacitacion.estado === filtroEstado.value

    return coincideBusqueda && coincideAnio && coincideEstado
  })
})

const totalEnCurso = computed(
  () => capacitaciones.value.filter((c) => c.estado === 'EN_CURSO').length,
)

const totalPlanificadas = computed(
  () => capacitaciones.value.filter((c) => c.estado === 'PLANIFICADA').length,
)

function obtenerMensajeError(err: unknown): string {
  if (typeof err === 'object' && err !== null && 'response' in err) {
    const respuesta = (
      err as {
        response?: {
          data?: { message?: string; detail?: string; error?: string }
        }
      }
    ).response

    return (
      respuesta?.data?.message ||
      respuesta?.data?.detail ||
      respuesta?.data?.error ||
      'No se pudo completar la operación.'
    )
  }

  return 'No se pudo conectar con el servidor. Inténtalo nuevamente.'
}

async function cargarCapacitaciones() {
  cargando.value = true
  error.value = ''

  try {
    capacitaciones.value = await capacitacionService.listar()
  } catch (err: unknown) {
    error.value = obtenerMensajeError(err)
  } finally {
    cargando.value = false
  }
}

function abrirModal() {
  erroresFormulario.value = {}

  formulario.value = {
    nombre: '',
    anio: new Date().getFullYear(),
    fechaInicio: '',
    fechaFin: '',
  }

  mensaje.value = ''
  error.value = ''
  modalAbierto.value = true
}

function cerrarModal() {
  if (guardando.value) return

  modalAbierto.value = false
  erroresFormulario.value = {}
}

function validarFormulario(): boolean {
  const nuevosErrores: Record<string, string> = {}
  const nombre = formulario.value.nombre.trim()
  const anio = formulario.value.anio

  if (!nombre) {
    nuevosErrores.nombre = 'El nombre es obligatorio.'
  } else if (nombre.length > 150) {
    nuevosErrores.nombre = 'El nombre no puede superar los 150 caracteres.'
  }

  if (!Number.isInteger(anio) || anio < 2000 || anio > 2100) {
    nuevosErrores.anio = 'Ingresa un año entre 2000 y 2100.'
  }

  if (!formulario.value.fechaInicio) {
    nuevosErrores.fechaInicio = 'La fecha de inicio es obligatoria.'
  }

  if (!formulario.value.fechaFin) {
    nuevosErrores.fechaFin = 'La fecha de fin es obligatoria.'
  }

  if (
    formulario.value.fechaInicio &&
    formulario.value.fechaFin &&
    formulario.value.fechaFin < formulario.value.fechaInicio
  ) {
    nuevosErrores.fechaFin = 'La fecha de fin no puede ser anterior a la fecha de inicio.'
  }

  erroresFormulario.value = nuevosErrores

  return Object.keys(nuevosErrores).length === 0
}

async function registrarCapacitacion() {
  mensaje.value = ''
  error.value = ''

  if (!validarFormulario()) return

  guardando.value = true

  try {
    const datos: CapacitacionRequest = {
      nombre: formulario.value.nombre.trim(),
      anio: formulario.value.anio,
      fechaInicio: formulario.value.fechaInicio,
      fechaFin: formulario.value.fechaFin,
    }

    await capacitacionService.registrar(datos)

    modalAbierto.value = false
    mensaje.value = 'La capacitación se registró correctamente.'

    await cargarCapacitaciones()
  } catch (err: unknown) {
    error.value = obtenerMensajeError(err)
  } finally {
    guardando.value = false
  }
}

async function iniciarCapacitacion(capacitacion: Capacitacion) {
  const confirmacion = await Swal.fire({
    title: '¿Iniciar capacitación?',
    html: `
      <p>Estás a punto de iniciar la capacitación:</p>
      <p style="margin-top: 10px; font-weight: 600;">
        ${capacitacion.nombre.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')}
      </p>
      <p style="margin-top: 10px; color: #64748b; font-size: 14px;">
        Esta acción cambiará su estado a EN CURSO.
      </p>
    `,
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Sí, iniciar',
    cancelButtonText: 'Cancelar',
    confirmButtonColor: '#17665e',
    cancelButtonColor: '#64748b',
    reverseButtons: true,
    focusCancel: true,
  })

  if (!confirmacion.isConfirmed) return

  try {
    await capacitacionService.iniciar(capacitacion.id)

    await Swal.fire({
      title: '¡Capacitación iniciada!',
      text: 'La capacitación ahora se encuentra en curso.',
      icon: 'success',
      confirmButtonText: 'Aceptar',
      confirmButtonColor: '#17665e',
    })

    await cargarCapacitaciones()
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
                    error?: string
                  }
                }
              }
            ).response?.data

            return (
              respuesta?.message ??
              respuesta?.detail ??
              respuesta?.error ??
              'No fue posible iniciar la capacitación.'
            )
          })()
        : 'No fue posible iniciar la capacitación.'

    await Swal.fire({
      title: 'No se pudo iniciar',
      text: mensaje,
      icon: 'error',
      confirmButtonText: 'Entendido',
      confirmButtonColor: '#17665e',
    })
  }
}

function etiquetaEstado(estado: EstadoCapacitacion): string {
  return estados.find((item) => item.valor === estado)?.etiqueta ?? estado
}

function claseEstado(estado: EstadoCapacitacion): string {
  const clases: Record<EstadoCapacitacion, string> = {
    PLANIFICADA: 'bg-blue-50 text-blue-700 ring-blue-600/20',
    EN_CURSO: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20',
    FINALIZADA: 'bg-slate-100 text-slate-700 ring-slate-600/20',
    CANCELADA: 'bg-red-50 text-red-700 ring-red-600/20',
  }

  return clases[estado]
}

function formatearFecha(fecha: string): string {
  if (!fecha) return '—'

  const [anio, mes, dia] = fecha.split('-')

  if (!anio || !mes || !dia) return fecha

  return `${dia}/${mes}/${anio}`
}

function limpiarFiltros() {
  busqueda.value = ''
  filtroAnio.value = ''
  filtroEstado.value = ''
}

onMounted(cargarCapacitaciones)
</script>

<template>
  <main class="min-h-full space-y-6 bg-[#edf5f3] p-4 sm:p-6 lg:p-8">
    <!-- Encabezado -->
    <section
      class="flex flex-col gap-4 rounded-2xl bg-[#17665e] p-6 text-white shadow-sm sm:flex-row sm:items-center sm:justify-between"
    >
      <div>
        <div class="mb-2 flex items-center gap-2 text-sm text-emerald-100">
          <BookOpen class="h-4 w-4" />
          <span>Administración académica</span>
        </div>

        <h1 class="text-2xl font-bold tracking-tight sm:text-3xl">Gestión de capacitaciones</h1>

        <p class="mt-2 max-w-2xl text-sm text-emerald-50">
          Administra las capacitaciones, consulta sus fechas y controla su estado.
        </p>
      </div>

      <button
        type="button"
        class="inline-flex items-center justify-center gap-2 rounded-xl bg-white px-4 py-3 text-sm font-semibold text-[#17665e] shadow-sm transition hover:bg-emerald-50 focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-[#17665e]"
        @click="abrirModal"
      >
        <Plus class="h-4 w-4" />
        Nueva capacitación
      </button>
    </section>

    <!-- Tarjetas de resumen -->
    <section class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
        <div class="flex items-center justify-between">
          <p class="text-sm font-medium text-slate-500">Total de capacitaciones</p>
          <span class="rounded-xl bg-[#e0f2ee] p-2 text-[#17665e]">
            <BookOpen class="h-5 w-5" />
          </span>
        </div>

        <p class="mt-4 text-3xl font-bold text-[#123f3b]">
          {{ capacitaciones.length }}
        </p>
        <p class="mt-1 text-xs text-slate-500">Registradas en el sistema</p>
      </article>

      <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
        <div class="flex items-center justify-between">
          <p class="text-sm font-medium text-slate-500">En curso</p>
          <span class="rounded-xl bg-emerald-50 p-2 text-emerald-700">
            <Clock3 class="h-5 w-5" />
          </span>
        </div>

        <p class="mt-4 text-3xl font-bold text-[#123f3b]">
          {{ totalEnCurso }}
        </p>
        <p class="mt-1 text-xs text-slate-500">Capacitaciones iniciadas</p>
      </article>

      <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
        <div class="flex items-center justify-between">
          <p class="text-sm font-medium text-slate-500">Planificadas</p>
          <span class="rounded-xl bg-blue-50 p-2 text-blue-700">
            <CalendarDays class="h-5 w-5" />
          </span>
        </div>

        <p class="mt-4 text-3xl font-bold text-[#123f3b]">
          {{ totalPlanificadas }}
        </p>
        <p class="mt-1 text-xs text-slate-500">Pendientes de iniciar</p>
      </article>
    </section>

    <!-- Listado -->
    <section class="overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white shadow-sm">
      <div
        class="flex flex-col gap-4 border-b border-[#dbe9e5] p-5 lg:flex-row lg:items-center lg:justify-between"
      >
        <div>
          <h2 class="text-lg font-bold text-[#123f3b]">Capacitaciones registradas</h2>
          <p class="mt-1 text-sm text-slate-500">
            Consulta y filtra las capacitaciones disponibles.
          </p>
        </div>

        <button
          type="button"
          class="inline-flex items-center justify-center gap-2 self-start rounded-xl border border-[#dbe9e5] px-3 py-2 text-sm font-medium text-[#17665e] transition hover:bg-[#edf5f3] disabled:cursor-not-allowed disabled:opacity-50 lg:self-auto"
          :disabled="cargando"
          @click="cargarCapacitaciones"
        >
          <RefreshCw class="h-4 w-4" :class="{ 'animate-spin': cargando }" />
          Actualizar
        </button>
      </div>

      <!-- Filtros -->
      <div class="grid gap-3 border-b border-[#dbe9e5] p-5 md:grid-cols-2 xl:grid-cols-3">
        <label class="relative block">
          <span class="sr-only">Buscar capacitación</span>
          <Search
            class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
          />
          <input
            v-model="busqueda"
            type="search"
            placeholder="Buscar por nombre o año..."
            class="w-full rounded-xl border border-[#dbe9e5] bg-white py-2.5 pl-9 pr-3 text-sm text-[#123f3b] outline-none transition placeholder:text-slate-400 focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
          />
        </label>

        <label class="block">
          <span class="sr-only">Filtrar por año</span>
          <select
            v-model="filtroAnio"
            class="w-full rounded-xl border border-[#dbe9e5] bg-white px-3 py-2.5 text-sm text-[#123f3b] outline-none focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
          >
            <option value="">Todos los años</option>
            <option v-for="anio in aniosDisponibles" :key="anio" :value="String(anio)">
              {{ anio }}
            </option>
          </select>
        </label>

        <label class="block">
          <span class="sr-only">Filtrar por estado</span>
          <select
            v-model="filtroEstado"
            class="w-full rounded-xl border border-[#dbe9e5] bg-white px-3 py-2.5 text-sm text-[#123f3b] outline-none focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
          >
            <option value="">Todos los estados</option>
            <option v-for="estado in estados" :key="estado.valor" :value="estado.valor">
              {{ estado.etiqueta }}
            </option>
          </select>
        </label>
      </div>

      <!-- Mensaje de éxito -->
      <div
        v-if="mensaje"
        role="status"
        class="mx-5 mt-5 flex items-start gap-2 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-800"
      >
        <CheckCircle2 class="mt-0.5 h-4 w-4 shrink-0" />
        <p>{{ mensaje }}</p>
        <button type="button" class="ml-auto" aria-label="Cerrar mensaje" @click="mensaje = ''">
          <X class="h-4 w-4" />
        </button>
      </div>

      <!-- Mensaje de error -->
      <div
        v-if="error"
        role="alert"
        class="mx-5 mt-5 flex items-start gap-2 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700"
      >
        <CircleAlert class="mt-0.5 h-4 w-4 shrink-0" />
        <p>{{ error }}</p>
        <button type="button" class="ml-auto" aria-label="Cerrar error" @click="error = ''">
          <X class="h-4 w-4" />
        </button>
      </div>

      <!-- Cargando -->
      <div v-if="cargando" class="flex flex-col items-center py-16 text-slate-500">
        <LoaderCircle class="h-8 w-8 animate-spin text-[#17665e]" />
        <p class="mt-3 text-sm">Cargando capacitaciones...</p>
      </div>

      <!-- Sin registros -->
      <div
        v-else-if="!error && capacitaciones.length === 0"
        class="flex flex-col items-center px-5 py-16 text-center"
      >
        <span class="rounded-2xl bg-[#e0f2ee] p-4 text-[#17665e]">
          <BookOpen class="h-8 w-8" />
        </span>

        <h3 class="mt-4 font-semibold text-[#123f3b]">Aún no hay capacitaciones</h3>

        <p class="mt-2 max-w-sm text-sm text-slate-500">
          Registra la primera capacitación para comenzar a organizar las actividades.
        </p>

        <button
          type="button"
          class="mt-5 inline-flex items-center gap-2 rounded-xl bg-[#17665e] px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-[#12564f]"
          @click="abrirModal"
        >
          <Plus class="h-4 w-4" />
          Registrar capacitación
        </button>
      </div>

      <!-- Sin resultados de búsqueda -->
      <div
        v-else-if="!error && capacitacionesFiltradas.length === 0"
        class="px-5 py-14 text-center"
      >
        <Search class="mx-auto h-8 w-8 text-slate-300" />

        <h3 class="mt-3 font-semibold text-[#123f3b]">No se encontraron resultados</h3>

        <p class="mt-1 text-sm text-slate-500">Prueba con otro nombre o modifica los filtros.</p>

        <button
          type="button"
          class="mt-3 text-sm font-semibold text-[#17665e] hover:underline"
          @click="limpiarFiltros"
        >
          Limpiar filtros
        </button>
      </div>

      <!-- Tabla -->
      <div v-else-if="!error" class="overflow-x-auto">
        <table class="w-full min-w-225 text-left text-sm">
          <thead class="bg-[#f5f9f8] text-xs uppercase tracking-wide text-slate-500">
            <tr>
              <th class="px-5 py-4 font-semibold">Capacitación</th>
              <th class="px-5 py-4 font-semibold">Año</th>
              <th class="px-5 py-4 font-semibold">Fecha de inicio</th>
              <th class="px-5 py-4 font-semibold">Fecha de fin</th>
              <th class="px-5 py-4 font-semibold">Estado</th>
              <th class="px-5 py-4 text-right font-semibold">Acciones</th>
            </tr>
          </thead>

          <tbody class="divide-y divide-[#e7efec]">
            <tr
              v-for="capacitacion in capacitacionesFiltradas"
              :key="capacitacion.id"
              class="transition hover:bg-[#f8fbfa]"
            >
              <td class="px-5 py-4">
                <div class="font-semibold text-[#123f3b]">
                  {{ capacitacion.nombre }}
                </div>
                <div class="mt-1 text-xs text-slate-400">ID: {{ capacitacion.id }}</div>
              </td>

              <td class="px-5 py-4 text-slate-600">
                {{ capacitacion.anio }}
              </td>

              <td class="whitespace-nowrap px-5 py-4 text-slate-600">
                {{ formatearFecha(capacitacion.fechaInicio) }}
              </td>

              <td class="whitespace-nowrap px-5 py-4 text-slate-600">
                {{ formatearFecha(capacitacion.fechaFin) }}
              </td>

              <td class="px-5 py-4">
                <span
                  class="inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset"
                  :class="claseEstado(capacitacion.estado)"
                >
                  {{ etiquetaEstado(capacitacion.estado) }}
                </span>
              </td>

              <td class="px-5 py-4 text-right">
                <button
                  v-if="capacitacion.estado === 'PLANIFICADA'"
                  type="button"
                  class="inline-flex items-center justify-center gap-2 rounded-lg bg-[#e0f2ee] px-3 py-2 text-xs font-semibold text-[#17665e] transition hover:bg-[#cce8e1] disabled:cursor-not-allowed disabled:opacity-50"
                  :disabled="iniciandoId !== null"
                  @click="iniciarCapacitacion(capacitacion)"
                >
                  <LoaderCircle
                    v-if="iniciandoId === capacitacion.id"
                    class="h-3.5 w-3.5 animate-spin"
                  />
                  <CheckCircle2 v-else class="h-3.5 w-3.5" />
                  Iniciar
                </button>

                <span
                  v-else-if="capacitacion.estado === 'EN_CURSO'"
                  class="text-xs font-medium text-emerald-700"
                >
                  En desarrollo
                </span>

                <span v-else class="text-xs text-slate-400">—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Conteo -->
      <div
        v-if="!cargando && !error && capacitacionesFiltradas.length > 0"
        class="border-t border-[#dbe9e5] px-5 py-3 text-xs text-slate-500"
      >
        Mostrando {{ capacitacionesFiltradas.length }} de {{ capacitaciones.length }} capacitaciones
      </div>
    </section>

    <!-- Modal de registro -->
    <Teleport to="body">
      <div
        v-if="modalAbierto"
        class="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-slate-950/50 p-4 backdrop-blur-sm"
        @click.self="cerrarModal"
        @keydown.esc="cerrarModal"
      >
        <section
          role="dialog"
          aria-modal="true"
          aria-labelledby="titulo-modal-capacitacion"
          class="my-auto w-full max-w-xl overflow-hidden rounded-2xl bg-white shadow-2xl"
        >
          <header
            class="flex items-start justify-between gap-4 border-b border-[#dbe9e5] bg-[#f5f9f8] p-5 sm:p-6"
          >
            <div>
              <span class="inline-flex rounded-xl bg-[#e0f2ee] p-2 text-[#17665e]">
                <BookOpen class="h-5 w-5" />
              </span>

              <h2 id="titulo-modal-capacitacion" class="mt-3 text-xl font-bold text-[#123f3b]">
                Nueva capacitación
              </h2>

              <p class="mt-1 text-sm text-slate-500">
                Completa los datos para registrar una capacitación.
              </p>
            </div>

            <button
              type="button"
              class="rounded-lg p-2 text-slate-400 transition hover:bg-white hover:text-slate-700"
              aria-label="Cerrar formulario"
              :disabled="guardando"
              @click="cerrarModal"
            >
              <X class="h-5 w-5" />
            </button>
          </header>

          <form class="space-y-5 p-5 sm:p-6" @submit.prevent="registrarCapacitacion">
            <div>
              <label
                for="nombre-capacitacion"
                class="mb-1.5 block text-sm font-semibold text-[#123f3b]"
              >
                Nombre de la capacitación
                <span class="text-red-500">*</span>
              </label>

              <input
                id="nombre-capacitacion"
                v-model="formulario.nombre"
                type="text"
                maxlength="150"
                required
                placeholder="Ej. Capacitación para aplicadores"
                class="w-full rounded-xl border border-[#dbe9e5] px-3.5 py-3 text-sm text-[#123f3b] outline-none transition placeholder:text-slate-400 focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
                :aria-invalid="Boolean(erroresFormulario.nombre)"
                :aria-describedby="
                  erroresFormulario.nombre ? 'error-nombre-capacitacion' : undefined
                "
              />

              <p
                v-if="erroresFormulario.nombre"
                id="error-nombre-capacitacion"
                class="mt-1.5 text-xs text-red-600"
              >
                {{ erroresFormulario.nombre }}
              </p>
            </div>

            <div>
              <label
                for="anio-capacitacion"
                class="mb-1.5 block text-sm font-semibold text-[#123f3b]"
              >
                Año
                <span class="text-red-500">*</span>
              </label>

              <input
                id="anio-capacitacion"
                v-model.number="formulario.anio"
                type="number"
                min="2000"
                max="2100"
                step="1"
                required
                class="w-full rounded-xl border border-[#dbe9e5] px-3.5 py-3 text-sm text-[#123f3b] outline-none transition focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
                :aria-invalid="Boolean(erroresFormulario.anio)"
              />

              <p v-if="erroresFormulario.anio" class="mt-1.5 text-xs text-red-600">
                {{ erroresFormulario.anio }}
              </p>
            </div>

            <div class="grid gap-4 sm:grid-cols-2">
              <div>
                <label
                  for="fecha-inicio-capacitacion"
                  class="mb-1.5 block text-sm font-semibold text-[#123f3b]"
                >
                  Fecha de inicio
                  <span class="text-red-500">*</span>
                </label>

                <input
                  id="fecha-inicio-capacitacion"
                  v-model="formulario.fechaInicio"
                  type="date"
                  required
                  class="w-full min-w-0 rounded-xl border border-[#dbe9e5] px-3 py-3 text-sm text-[#123f3b] outline-none transition focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
                  :aria-invalid="Boolean(erroresFormulario.fechaInicio)"
                />

                <p v-if="erroresFormulario.fechaInicio" class="mt-1.5 text-xs text-red-600">
                  {{ erroresFormulario.fechaInicio }}
                </p>
              </div>

              <div>
                <label
                  for="fecha-fin-capacitacion"
                  class="mb-1.5 block text-sm font-semibold text-[#123f3b]"
                >
                  Fecha de fin
                  <span class="text-red-500">*</span>
                </label>

                <input
                  id="fecha-fin-capacitacion"
                  v-model="formulario.fechaFin"
                  type="date"
                  required
                  class="w-full min-w-0 rounded-xl border border-[#dbe9e5] px-3 py-3 text-sm text-[#123f3b] outline-none transition focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
                  :aria-invalid="Boolean(erroresFormulario.fechaFin)"
                />

                <p v-if="erroresFormulario.fechaFin" class="mt-1.5 text-xs text-red-600">
                  {{ erroresFormulario.fechaFin }}
                </p>
              </div>
            </div>

            <div
              class="rounded-xl border border-[#dbe9e5] bg-[#f5f9f8] p-3.5 text-xs leading-5 text-slate-600"
            >
              El estado inicial se asigna automáticamente según las reglas del backend. La
              capacitación podrá iniciarse desde la tabla cuando corresponda.
            </div>

            <div
              class="flex flex-col-reverse gap-3 border-t border-[#e7efec] pt-5 sm:flex-row sm:justify-end"
            >
              <button
                type="button"
                class="rounded-xl border border-[#dbe9e5] px-4 py-2.5 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 disabled:opacity-50"
                :disabled="guardando"
                @click="cerrarModal"
              >
                Cancelar
              </button>

              <button
                type="submit"
                class="inline-flex items-center justify-center gap-2 rounded-xl bg-[#17665e] px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-[#12564f] disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="guardando"
              >
                <LoaderCircle v-if="guardando" class="h-4 w-4 animate-spin" />
                <Plus v-else class="h-4 w-4" />
                {{ guardando ? 'Registrando...' : 'Registrar capacitación' }}
              </button>
            </div>
          </form>
        </section>
      </div>
    </Teleport>
  </main>
</template>
