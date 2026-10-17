<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  BookOpen,
  CheckCircle2,
  CircleAlert,
  DoorOpen,
  LoaderCircle,
  Plus,
  RefreshCw,
  Search,
  UsersRound,
  X,
} from 'lucide-vue-next'

import { aulaService } from '@/services/aulaService'
import type { Aula, AulaRequest, TipoAula } from '@/types/aula'

const aulas = ref<Aula[]>([])
const cargando = ref(false)
const guardando = ref(false)
const error = ref('')
const mensaje = ref('')

const busqueda = ref('')
const filtroTipo = ref<'TODOS' | TipoAula>('TODOS')
const filtroEstado = ref<'TODOS' | 'ACTIVAS' | 'INACTIVAS'>('TODOS')

const modalAbierto = ref(false)

const formulario = ref<AulaRequest>({
  codigo: '',
  tipo: 'APLICADOR',
  capacidad: 30,
})

const erroresFormulario = ref<Record<string, string>>({})

const totalAulas = computed(() => aulas.value.length)

const totalAplicador = computed(
  () => aulas.value.filter((aula) => aula.tipo === 'APLICADOR').length,
)

const totalOrientador = computed(
  () => aulas.value.filter((aula) => aula.tipo === 'ORIENTADOR').length,
)

const aulasFiltradas = computed(() => {
  const termino = busqueda.value.trim().toLocaleLowerCase()

  return aulas.value.filter((aula) => {
    const coincideCodigo = aula.codigo.toLocaleLowerCase().includes(termino)

    const coincideTipo = filtroTipo.value === 'TODOS' || aula.tipo === filtroTipo.value

    const coincideEstado =
      filtroEstado.value === 'TODOS' ||
      (filtroEstado.value === 'ACTIVAS' && aula.activa) ||
      (filtroEstado.value === 'INACTIVAS' && !aula.activa)

    return coincideCodigo && coincideTipo && coincideEstado
  })
})

async function cargarAulas() {
  cargando.value = true
  error.value = ''

  try {
    aulas.value = await aulaService.listar()
  } catch (err: unknown) {
    console.error('Error al cargar las aulas:', err)
    error.value = 'No fue posible cargar las aulas. Verifica la conexión con el servidor.'
  } finally {
    cargando.value = false
  }
}

function abrirModal() {
  formulario.value = {
    codigo: '',
    tipo: 'APLICADOR',
    capacidad: 30,
  }

  erroresFormulario.value = {}
  mensaje.value = ''
  modalAbierto.value = true
}

function cerrarModal() {
  if (guardando.value) return
  modalAbierto.value = false
}

function validarFormulario(): boolean {
  const errores: Record<string, string> = {}

  const codigo = formulario.value.codigo.trim()

  if (!codigo) {
    errores.codigo = 'El código es obligatorio.'
  } else if (codigo.length > 30) {
    errores.codigo = 'El código no puede superar los 30 caracteres.'
  } else if (!/^[a-zA-Z0-9_-]+$/.test(codigo)) {
    errores.codigo = 'Utiliza solo letras, números, guiones o guiones bajos.'
  }

  const capacidad = Number(formulario.value.capacidad)

  if (!Number.isInteger(capacidad) || capacidad < 1 || capacidad > 500) {
    errores.capacidad = 'La capacidad debe ser un número entero entre 1 y 500.'
  }

  erroresFormulario.value = errores

  return Object.keys(errores).length === 0
}

async function registrarAula() {
  mensaje.value = ''
  error.value = ''

  if (!validarFormulario()) return

  guardando.value = true

  try {
    const solicitud: AulaRequest = {
      codigo: formulario.value.codigo.trim().toUpperCase(),
      tipo: formulario.value.tipo,
      capacidad: Number(formulario.value.capacidad),
    }

    await aulaService.registrar(solicitud)

    modalAbierto.value = false
    mensaje.value = 'El aula se registró correctamente.'

    await cargarAulas()
  } catch (err: unknown) {
    console.error('Error al registrar el aula:', err)

    const errorHttp = err as {
      response?: {
        status?: number
        data?: {
          message?: string
          mensaje?: string
          detail?: string
        }
      }
    }

    if (errorHttp.response?.status === 409) {
      error.value = 'Ya existe un aula con ese código.'
    } else {
      error.value =
        errorHttp.response?.data?.message ??
        errorHttp.response?.data?.mensaje ??
        errorHttp.response?.data?.detail ??
        'No fue posible registrar el aula. Verifica los datos e inténtalo nuevamente.'
    }
  } finally {
    guardando.value = false
  }
}

function limpiarFiltros() {
  busqueda.value = ''
  filtroTipo.value = 'TODOS'
  filtroEstado.value = 'TODOS'
}

function formatoFecha(fecha: string): string {
  if (!fecha) return '—'

  const fechaValida = new Date(fecha)

  if (Number.isNaN(fechaValida.getTime())) return '—'

  return new Intl.DateTimeFormat('es-PE', {
    dateStyle: 'medium',
    timeZone: 'America/Lima',
  }).format(fechaValida)
}

onMounted(cargarAulas)
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
          Gestión académica
        </div>

        <h1 class="text-2xl font-bold tracking-tight sm:text-3xl">Gestión de aulas</h1>

        <p class="mt-2 max-w-2xl text-sm leading-6 text-emerald-50">
          Administra las aulas de capacitación, consulta su capacidad y organiza los espacios para
          APLICADOR y ORIENTADOR.
        </p>
      </div>

      <button
        type="button"
        class="inline-flex shrink-0 items-center justify-center gap-2 rounded-xl bg-white px-5 py-3 text-sm font-semibold text-[#17665e] shadow-sm transition hover:bg-emerald-50 disabled:cursor-not-allowed disabled:opacity-60"
        @click="abrirModal"
      >
        <Plus class="h-5 w-5" />
        Nueva aula
      </button>
    </section>

    <!-- Mensajes -->
    <div
      v-if="mensaje"
      role="status"
      class="flex items-start gap-3 rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-800"
    >
      <CheckCircle2 class="mt-0.5 h-5 w-5 shrink-0" />
      <p>{{ mensaje }}</p>
      <button type="button" class="ml-auto" aria-label="Cerrar mensaje" @click="mensaje = ''">
        <X class="h-4 w-4" />
      </button>
    </div>

    <div
      v-if="error"
      role="alert"
      class="flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800"
    >
      <CircleAlert class="mt-0.5 h-5 w-5 shrink-0" />
      <p>{{ error }}</p>
      <button type="button" class="ml-auto" aria-label="Cerrar error" @click="error = ''">
        <X class="h-4 w-4" />
      </button>
    </div>

    <!-- Indicadores -->
    <section class="grid grid-cols-1 gap-4 sm:grid-cols-3">
      <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
        <div class="flex items-center justify-between gap-4">
          <div>
            <p class="text-sm font-medium text-slate-500">Total de aulas</p>
            <p class="mt-2 text-3xl font-bold text-[#123f3b]">
              {{ totalAulas }}
            </p>
          </div>

          <div class="rounded-xl bg-[#e0f2ee] p-3 text-[#17665e]">
            <DoorOpen class="h-6 w-6" />
          </div>
        </div>
        <p class="mt-3 text-xs text-slate-500">Aulas registradas</p>
      </article>

      <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
        <div class="flex items-center justify-between gap-4">
          <div>
            <p class="text-sm font-medium text-slate-500">Tipo APLICADOR</p>
            <p class="mt-2 text-3xl font-bold text-[#123f3b]">
              {{ totalAplicador }}
            </p>
          </div>

          <div class="rounded-xl bg-[#e0f2ee] p-3 text-[#17665e]">
            <UsersRound class="h-6 w-6" />
          </div>
        </div>
        <p class="mt-3 text-xs text-slate-500">Aulas de aplicadores</p>
      </article>

      <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
        <div class="flex items-center justify-between gap-4">
          <div>
            <p class="text-sm font-medium text-slate-500">Tipo ORIENTADOR</p>
            <p class="mt-2 text-3xl font-bold text-[#123f3b]">
              {{ totalOrientador }}
            </p>
          </div>

          <div class="rounded-xl bg-[#e0f2ee] p-3 text-[#17665e]">
            <UsersRound class="h-6 w-6" />
          </div>
        </div>
        <p class="mt-3 text-xs text-slate-500">Aulas de orientadores</p>
      </article>
    </section>

    <!-- Tabla -->
    <section class="overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white shadow-sm">
      <div
        class="flex flex-col gap-4 border-b border-[#e5eeeb] p-5 lg:flex-row lg:items-center lg:justify-between"
      >
        <div>
          <h2 class="text-lg font-bold text-[#123f3b]">Aulas registradas</h2>
          <p class="mt-1 text-sm text-slate-500">
            Consulta y filtra las aulas disponibles en el sistema.
          </p>
        </div>
      </div>

      <!-- Filtros -->
      <div class="grid grid-cols-1 gap-3 border-b border-[#e5eeeb] p-5 md:grid-cols-3">
        <div class="relative">
          <Search
            class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
          />
          <input
            v-model="busqueda"
            type="search"
            placeholder="Buscar por código..."
            class="w-full rounded-xl border border-[#dbe9e5] bg-white py-3 pl-10 pr-4 text-sm text-[#123f3b] outline-none transition placeholder:text-slate-400 focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
          />
        </div>

        <select
          v-model="filtroTipo"
          aria-label="Filtrar por tipo de aula"
          class="w-full rounded-xl border border-[#dbe9e5] bg-white px-4 py-3 text-sm text-[#123f3b] outline-none focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
        >
          <option value="TODOS">Todos los tipos</option>
          <option value="APLICADOR">Aplicador</option>
          <option value="ORIENTADOR">Orientador</option>
        </select>

        <select
          v-model="filtroEstado"
          aria-label="Filtrar por estado"
          class="w-full rounded-xl border border-[#dbe9e5] bg-white px-4 py-3 text-sm text-[#123f3b] outline-none focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
        >
          <option value="TODOS">Todos los estados</option>
          <option value="ACTIVAS">Activas</option>
          <option value="INACTIVAS">Inactivas</option>
        </select>
      </div>

      <div class="flex flex-wrap items-center justify-between gap-3 px-5 py-4">
        <p class="text-sm text-slate-500">
          Mostrando
          <span class="font-semibold text-[#123f3b]">
            {{ aulasFiltradas.length }}
          </span>
          de
          <span class="font-semibold text-[#123f3b]">{{ totalAulas }}</span>
          aulas
        </p>

        <button
          v-if="busqueda || filtroTipo !== 'TODOS' || filtroEstado !== 'TODOS'"
          type="button"
          class="text-sm font-semibold text-[#17665e] hover:underline"
          @click="limpiarFiltros"
        >
          Limpiar filtros
        </button>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full min-w-190 text-left text-sm">
          <thead class="bg-[#f5faf8] text-xs uppercase tracking-wide text-slate-500">
            <tr>
              <th class="px-5 py-4 font-semibold">Código</th>
              <th class="px-5 py-4 font-semibold">Tipo de aula</th>
              <th class="px-5 py-4 font-semibold">Capacidad</th>
              <th class="px-5 py-4 font-semibold">Estado</th>
              <th class="px-5 py-4 font-semibold">Fecha de registro</th>
            </tr>
          </thead>

          <tbody class="divide-y divide-[#edf3f1]">
            <tr v-if="cargando">
              <td colspan="5" class="px-5 py-14 text-center">
                <LoaderCircle class="mx-auto h-7 w-7 animate-spin text-[#17665e]" />
                <p class="mt-3 text-sm text-slate-500">Cargando aulas...</p>
              </td>
            </tr>

            <tr v-else-if="!error && aulasFiltradas.length === 0">
              <td colspan="5" class="px-5 py-14 text-center">
                <div
                  class="mx-auto flex h-12 w-12 items-center justify-center rounded-xl bg-[#e0f2ee] text-[#17665e]"
                >
                  <DoorOpen class="h-6 w-6" />
                </div>
                <p class="mt-3 font-semibold text-[#123f3b]">No se encontraron aulas</p>
                <p class="mt-1 text-sm text-slate-500">
                  Registra una nueva aula o modifica los filtros de búsqueda.
                </p>
              </td>
            </tr>

            <tr v-for="aula in aulasFiltradas" :key="aula.id" class="transition hover:bg-[#f8fbfa]">
              <td class="px-5 py-4">
                <span class="font-bold text-[#17665e]">
                  {{ aula.codigo }}
                </span>
              </td>

              <td class="px-5 py-4">
                <span
                  class="inline-flex rounded-full px-3 py-1 text-xs font-semibold"
                  :class="
                    aula.tipo === 'APLICADOR'
                      ? 'bg-[#e0f2ee] text-[#17665e]'
                      : 'bg-violet-50 text-violet-700'
                  "
                >
                  {{ aula.tipo }}
                </span>
              </td>

              <td class="px-5 py-4">
                <span class="inline-flex items-center gap-2 text-slate-700">
                  <UsersRound class="h-4 w-4 text-slate-400" />
                  {{ aula.capacidad }} personas
                </span>
              </td>

              <td class="px-5 py-4">
                <span
                  class="inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-semibold"
                  :class="
                    aula.activa ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-600'
                  "
                >
                  <span
                    class="h-1.5 w-1.5 rounded-full"
                    :class="aula.activa ? 'bg-emerald-500' : 'bg-slate-400'"
                  />
                  {{ aula.activa ? 'Activa' : 'Inactiva' }}
                </span>
              </td>

              <td class="px-5 py-4 text-slate-500">
                {{ formatoFecha(aula.fechaCreacion) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div
        class="flex items-center gap-2 border-t border-[#e5eeeb] bg-[#f5faf8] px-5 py-3 text-xs text-slate-500"
      >
        <CheckCircle2 class="h-4 w-4 text-[#17665e]" />
        Los datos se obtienen del microservicio de aulas.
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
          aria-labelledby="titulo-modal-aula"
          class="my-auto w-full max-w-lg overflow-hidden rounded-2xl bg-white shadow-2xl"
        >
          <header class="flex items-start justify-between gap-4 bg-[#17665e] p-6 text-white">
            <div>
              <h2 id="titulo-modal-aula" class="text-xl font-bold">Registrar nueva aula</h2>
              <p class="mt-1 text-sm text-emerald-50">
                Completa los datos del aula de capacitación.
              </p>
            </div>

            <button
              type="button"
              class="rounded-lg p-2 transition hover:bg-white/10 disabled:opacity-50"
              aria-label="Cerrar formulario"
              :disabled="guardando"
              @click="cerrarModal"
            >
              <X class="h-5 w-5" />
            </button>
          </header>

          <form class="space-y-5 p-6" @submit.prevent="registrarAula">
            <div>
              <label for="codigo-aula" class="mb-2 block text-sm font-semibold text-[#123f3b]">
                Código del aula <span class="text-red-500">*</span>
              </label>

              <input
                id="codigo-aula"
                v-model="formulario.codigo"
                type="text"
                maxlength="30"
                autocomplete="off"
                placeholder="Ej. APL-001"
                :aria-invalid="Boolean(erroresFormulario.codigo)"
                class="w-full rounded-xl border border-[#dbe9e5] px-4 py-3 text-sm uppercase text-[#123f3b] outline-none transition placeholder:normal-case placeholder:text-slate-400 focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
                :class="erroresFormulario.codigo ? 'border-red-400' : ''"
              />

              <p v-if="erroresFormulario.codigo" class="mt-1.5 text-xs text-red-600">
                {{ erroresFormulario.codigo }}
              </p>
            </div>

            <div>
              <label for="tipo-aula" class="mb-2 block text-sm font-semibold text-[#123f3b]">
                Tipo de aula <span class="text-red-500">*</span>
              </label>

              <select
                id="tipo-aula"
                v-model="formulario.tipo"
                class="w-full rounded-xl border border-[#dbe9e5] bg-white px-4 py-3 text-sm text-[#123f3b] outline-none focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
              >
                <option value="APLICADOR">Aplicador</option>
                <option value="ORIENTADOR">Orientador</option>
              </select>
            </div>

            <div>
              <label for="capacidad-aula" class="mb-2 block text-sm font-semibold text-[#123f3b]">
                Capacidad máxima <span class="text-red-500">*</span>
              </label>

              <input
                id="capacidad-aula"
                v-model.number="formulario.capacidad"
                type="number"
                min="1"
                max="500"
                step="1"
                :aria-invalid="Boolean(erroresFormulario.capacidad)"
                class="w-full rounded-xl border border-[#dbe9e5] px-4 py-3 text-sm text-[#123f3b] outline-none focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
                :class="erroresFormulario.capacidad ? 'border-red-400' : ''"
              />

              <p class="mt-1.5 text-xs text-slate-500">Ingresa un número entero entre 1 y 500.</p>

              <p v-if="erroresFormulario.capacidad" class="mt-1.5 text-xs text-red-600">
                {{ erroresFormulario.capacidad }}
              </p>
            </div>

            <div class="rounded-xl border border-[#dbe9e5] bg-[#f5faf8] p-4">
              <div class="flex items-start gap-3">
                <div class="rounded-lg bg-[#e0f2ee] p-2 text-[#17665e]">
                  <DoorOpen class="h-5 w-5" />
                </div>

                <div>
                  <p class="text-sm font-semibold text-[#123f3b]">Aula activa</p>
                  <p class="mt-1 text-xs leading-5 text-slate-500">
                    El backend registrará el aula con su estado predeterminado.
                  </p>
                </div>
              </div>
            </div>

            <div
              class="flex flex-col-reverse gap-3 border-t border-[#edf3f1] pt-5 sm:flex-row sm:justify-end"
            >
              <button
                type="button"
                class="rounded-xl border border-[#dbe9e5] px-5 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 disabled:opacity-50"
                :disabled="guardando"
                @click="cerrarModal"
              >
                Cancelar
              </button>

              <button
                type="submit"
                class="inline-flex items-center justify-center gap-2 rounded-xl bg-[#17665e] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#12544d] disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="guardando"
              >
                <LoaderCircle v-if="guardando" class="h-4 w-4 animate-spin" />
                <Plus v-else class="h-4 w-4" />
                {{ guardando ? 'Registrando...' : 'Registrar aula' }}
              </button>
            </div>
          </form>
        </section>
      </div>
    </Teleport>
  </main>
</template>
