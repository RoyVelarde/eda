<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import {
  AlertCircle,
  BriefcaseBusiness,
  CheckCircle2,
  ChevronDown,
  Eye,
  FileSpreadsheet,
  LoaderCircle,
  Mail,
  RefreshCw,
  Search,
  Upload,
  UsersRound,
  X,
} from 'lucide-vue-next'

import { useRouter } from 'vue-router'
import { descargarPlantillaExcel } from '@/services/plantillaExcelService'
import { personalService, type ImportacionResponse } from '@/services/personalService'
import type { NivelPersonal, Personal } from '@/types/personal'

const personal = ref<Personal[]>([])
const cargando = ref(false)
const error = ref('')
const busqueda = ref('')
const nivelSeleccionado = ref<'TODOS' | NivelPersonal>('TODOS')
const cargoSeleccionado = ref('TODOS')

// Estado del modal y de la importación.
const modalImportacionAbierto = ref(false)
const nivelImportacion = ref<'NIVEL_2' | 'NIVEL_3'>('NIVEL_2')
const archivoImportacion = ref<File | null>(null)
const importando = ref(false)
const errorImportacion = ref('')
const resultadoImportacion = ref<ImportacionResponse | null>(null)
const router = useRouter()

function verDetalle(id: string) {
  void router.push({
    name: 'personal-detalle',
    params: { id },
  })
}

async function descargarPlantillaNivel2() {
  try {
    await descargarPlantillaExcel('NIVEL_2')
  } catch (error) {
    console.error('Error al descargar la plantilla de Nivel 2:', error)
  }
}

async function descargarPlantillaNivel3() {
  try {
    await descargarPlantillaExcel('NIVEL_3')
  } catch (error) {
    console.error('Error al descargar la plantilla de Nivel 3:', error)
  }
}

const niveles = [
  { valor: 'TODOS', etiqueta: 'Todos los niveles' },
  { valor: 'NIVEL_2', etiqueta: 'Nivel 2' },
  { valor: 'NIVEL_3', etiqueta: 'Nivel 3' },
] as const

const cargos = computed(() => {
  const valores = [...new Set(personal.value.map((persona) => persona.cargo))]
    .filter(Boolean)
    .sort((a, b) => a.localeCompare(b, 'es'))

  return [
    { valor: 'TODOS', etiqueta: 'Todos los cargos' },
    ...valores.map((cargo) => ({ valor: cargo, etiqueta: cargo })),
  ]
})

const totalNivel2 = computed(
  () => personal.value.filter((persona) => persona.nivel === 'NIVEL_2').length,
)

const totalNivel3 = computed(
  () => personal.value.filter((persona) => persona.nivel === 'NIVEL_3').length,
)

const personalFiltrado = computed(() => {
  const termino = busqueda.value.trim().toLocaleLowerCase('es')

  return personal.value.filter((persona) => {
    const coincideNivel =
      nivelSeleccionado.value === 'TODOS' || persona.nivel === nivelSeleccionado.value

    const coincideCargo =
      cargoSeleccionado.value === 'TODOS' || persona.cargo === cargoSeleccionado.value

    const texto = [
      persona.dni,
      persona.nombres,
      persona.apellidoPaterno,
      persona.apellidoMaterno,
      persona.correo ?? '',
      persona.cargo,
    ]
      .join(' ')
      .toLocaleLowerCase('es')

    return coincideNivel && coincideCargo && texto.includes(termino)
  })
})

async function cargarPersonal() {
  cargando.value = true
  error.value = ''

  try {
    personal.value = await personalService.listar()
  } catch (e: unknown) {
    personal.value = []

    if (
      typeof e === 'object' &&
      e !== null &&
      'response' in e &&
      typeof e.response === 'object' &&
      e.response !== null &&
      'status' in e.response
    ) {
      const status = e.response.status

      if (status === 401 || status === 403) {
        error.value =
          'No tienes autorización para consultar el personal. Verifica tu sesión y los permisos asignados.'
      } else {
        error.value = `No se pudo cargar el personal (HTTP ${String(status)}). Intenta nuevamente.`
      }
    } else {
      error.value =
        'No se pudo conectar con el servidor. Comprueba que el Gateway y personal-service estén ejecutándose.'
    }
  } finally {
    cargando.value = false
  }
}

function abrirModalImportacion() {
  errorImportacion.value = ''
  resultadoImportacion.value = null
  modalImportacionAbierto.value = true
}

function cerrarModalImportacion() {
  if (importando.value) return
  modalImportacionAbierto.value = false
}

function manejarEscape(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    cerrarModalImportacion()
  }
}

function seleccionarArchivo(event: Event) {
  const input = event.target as HTMLInputElement
  const archivo = input.files?.[0] ?? null

  errorImportacion.value = ''
  resultadoImportacion.value = null

  if (archivo && !archivo.name.toLowerCase().endsWith('.xlsx')) {
    errorImportacion.value = 'Selecciona un archivo Excel válido con extensión .xlsx.'

    archivoImportacion.value = null
    input.value = ''
    return
  }

  archivoImportacion.value = archivo
}

async function importarExcel() {
  if (!archivoImportacion.value) {
    errorImportacion.value = 'Selecciona un archivo Excel antes de continuar.'
    return
  }

  importando.value = true
  errorImportacion.value = ''
  resultadoImportacion.value = null

  try {
    const resultado = await personalService.importarExcel(
      archivoImportacion.value,
      nivelImportacion.value,
    )

    resultadoImportacion.value = resultado

    if (resultado.valido && resultado.filasConError === 0) {
      await cargarPersonal()
      archivoImportacion.value = null
    }
  } catch (e: unknown) {
    const errorApi = e as {
      response?: {
        data?: {
          message?: string
          mensaje?: string
          error?: string
        }
      }
    }

    errorImportacion.value =
      errorApi.response?.data?.mensaje ??
      errorApi.response?.data?.message ??
      errorApi.response?.data?.error ??
      'No se pudo importar el archivo. Verifica la conexión e inténtalo nuevamente.'
  } finally {
    importando.value = false
  }
}

function limpiarFiltros() {
  busqueda.value = ''
  nivelSeleccionado.value = 'TODOS'
  cargoSeleccionado.value = 'TODOS'
}

function nombreCompleto(persona: Personal) {
  return [persona.apellidoPaterno, persona.apellidoMaterno, persona.nombres]
    .filter(Boolean)
    .join(' ')
}

onMounted(() => {
  void cargarPersonal()
  window.addEventListener('keydown', manejarEscape)
})

onUnmounted(() => {
  window.removeEventListener('keydown', manejarEscape)
})
</script>

<template>
  <main class="min-h-full space-y-6 bg-[#edf5f3] p-4 sm:p-6 lg:p-8">
    <!-- Encabezado -->
    <header
      class="mb-8 flex flex-col gap-4 rounded-2xl bg-[#17665e] p-6 text-white shadow-sm sm:flex-row sm:items-center sm:justify-between"
    >
      <div>
        <div class="mb-2 flex items-center gap-2 text-sm text-emerald-100">
          <UsersRound :size="18" />
          Gestión del personal
        </div>

        <h1 class="text-2xl font-bold tracking-tight sm:text-3xl">Personal</h1>

        <p class="mt-2 max-w-2xl text-sm leading-6 text-emerald-50">
          Consulta y administra los registros del personal de los niveles 2 y 3.
        </p>
      </div>

      <div class="flex flex-wrap items-center gap-3">
        <button
          type="button"
          class="inline-flex items-center justify-center gap-2 rounded-xl bg-white px-5 py-3 text-sm font-semibold text-[#17665e] shadow-sm transition hover:bg-emerald-50"
          @click="abrirModalImportacion"
        >
          <Upload :size="18" />
          Agregar personal
        </button>
      </div>
    </header>

    <!-- Indicadores -->
    <section class="mb-8 grid gap-4 sm:grid-cols-3">
      <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
        <div class="flex items-center justify-between">
          <p class="text-sm font-medium text-slate-500">Total de registros</p>
          <div class="rounded-xl bg-[#e0f2ee] p-2 text-[#17665e]">
            <UsersRound :size="20" />
          </div>
        </div>
        <p class="mt-3 text-3xl font-bold text-[#123f3b]">
          {{ personal.length }}
        </p>
        <p class="mt-1 text-xs text-slate-500">Personal registrado</p>
      </article>

      <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
        <div class="flex items-center justify-between">
          <p class="text-sm font-medium text-slate-500">Nivel 2</p>
          <div class="rounded-xl bg-emerald-50 p-2 text-emerald-700">
            <BriefcaseBusiness :size="20" />
          </div>
        </div>
        <p class="mt-3 text-3xl font-bold text-[#123f3b]">
          {{ totalNivel2 }}
        </p>
        <p class="mt-1 text-xs text-slate-500">Personal de nivel 2</p>
      </article>

      <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
        <div class="flex items-center justify-between">
          <p class="text-sm font-medium text-slate-500">Nivel 3</p>
          <div class="rounded-xl bg-teal-50 p-2 text-teal-700">
            <UsersRound :size="20" />
          </div>
        </div>
        <p class="mt-3 text-3xl font-bold text-[#123f3b]">
          {{ totalNivel3 }}
        </p>
        <p class="mt-1 text-xs text-slate-500">Personal de nivel 3</p>
      </article>
    </section>

    <!-- Directorio y filtros -->
    <section class="overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white shadow-sm">
      <div class="border-b border-[#e7efec] p-5 sm:p-6">
        <div class="mb-5 flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
          <div>
            <h2 class="text-lg font-bold text-[#123f3b]">Directorio de personal</h2>
            <p class="mt-1 text-sm text-slate-500">
              {{ personalFiltrado.length }} de {{ personal.length }} registros
            </p>
          </div>
          <button
            v-if="busqueda || nivelSeleccionado !== 'TODOS' || cargoSeleccionado !== 'TODOS'"
            type="button"
            class="inline-flex items-center gap-2 self-start text-sm font-medium text-[#17665e] hover:text-[#123f3b]"
            @click="limpiarFiltros"
          >
            <X :size="16" />
            Limpiar filtros
          </button>
        </div>

        <div class="grid gap-3 md:grid-cols-3">
          <!-- Búsqueda -->
          <label class="relative block md:col-span-1">
            <Search :size="18" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              v-model="busqueda"
              type="search"
              placeholder="Buscar por DNI, nombre o correo..."
              class="w-full rounded-xl border border-[#dbe9e5] bg-[#fbfdfc] py-3 pl-10 pr-4 text-sm outline-none transition placeholder:text-slate-400 focus:border-[#17665e] focus:ring-4 focus:ring-[#17665e]/10"
            />
          </label>

          <!-- Filtro nivel -->
          <label class="relative block">
            <select
              v-model="nivelSeleccionado"
              class="w-full appearance-none rounded-xl border border-[#dbe9e5] bg-[#fbfdfc] px-4 py-3 pr-10 text-sm text-slate-700 outline-none focus:border-[#17665e] focus:ring-4 focus:ring-[#17665e]/10"
            >
              <option v-for="nivel in niveles" :key="nivel.valor" :value="nivel.valor">
                {{ nivel.etiqueta }}
              </option>
            </select>
            <ChevronDown
              :size="17"
              class="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-slate-400"
            />
          </label>

          <!-- Filtro cargo -->
          <label class="relative block">
            <select
              v-model="cargoSeleccionado"
              class="w-full appearance-none rounded-xl border border-[#dbe9e5] bg-[#fbfdfc] px-4 py-3 pr-10 text-sm text-slate-700 outline-none focus:border-[#17665e] focus:ring-4 focus:ring-[#17665e]/10"
            >
              <option v-for="cargo in cargos" :key="cargo.valor" :value="cargo.valor">
                {{ cargo.etiqueta }}
              </option>
            </select>
            <ChevronDown
              :size="17"
              class="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-slate-400"
            />
          </label>
        </div>
      </div>

      <!-- Cargando -->
      <div v-if="cargando" class="flex flex-col items-center px-6 py-16 text-center">
        <LoaderCircle :size="35" class="animate-spin text-[#17665e]" />
        <p class="mt-4 font-semibold text-[#123f3b]">Cargando personal...</p>
        <p class="mt-1 text-sm text-slate-500">Consultando el servidor.</p>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="flex flex-col items-center px-6 py-16 text-center">
        <div class="rounded-2xl bg-red-50 p-4 text-red-600">
          <AlertCircle :size="30" />
        </div>
        <h3 class="mt-4 font-bold text-[#123f3b]">No se pudo cargar la información</h3>
        <p class="mt-2 max-w-lg text-sm leading-6 text-slate-500">
          {{ error }}
        </p>
        <button
          type="button"
          class="mt-5 inline-flex items-center gap-2 rounded-xl bg-[#17665e] px-5 py-3 text-sm font-semibold text-white hover:bg-[#123f3b]"
          @click="cargarPersonal"
        >
          <RefreshCw :size="17" />
          Intentar nuevamente
        </button>
      </div>

      <!-- Tabla -->
      <div v-else-if="personalFiltrado.length" class="overflow-x-auto">
        <table class="w-full min-w-237.5 text-left text-sm">
          <thead class="bg-[#f5faf8] text-xs uppercase tracking-wide text-slate-500">
            <tr>
              <th class="px-5 py-4 font-semibold">Personal</th>
              <th class="px-5 py-4 font-semibold">DNI</th>
              <th class="px-5 py-4 font-semibold">Nivel</th>
              <th class="px-5 py-4 font-semibold">Cargo</th>
              <th class="px-5 py-4 font-semibold">Contacto</th>
              <th class="px-5 py-4 font-semibold">Situación</th>
              <th class="px-5 py-4 font-semibold">Ranking</th>
              <th class="px-5 py-4 text-right font-semibold">Acciones</th>
            </tr>
          </thead>

          <tbody class="divide-y divide-[#edf2f0]">
            <tr
              v-for="persona in personalFiltrado"
              :key="persona.id"
              class="transition hover:bg-[#f8fbfa]"
            >
              <td class="px-5 py-4">
                <p class="font-semibold text-[#123f3b]">
                  {{ nombreCompleto(persona) }}
                </p>
                <p class="mt-1 text-xs text-slate-500">
                  {{ persona.correo || 'Sin correo registrado' }}
                </p>
              </td>

              <td class="px-5 py-4 font-medium text-slate-700">
                {{ persona.dni }}
              </td>

              <td class="px-5 py-4">
                <span
                  class="inline-flex rounded-lg px-2.5 py-1 text-xs font-semibold"
                  :class="
                    persona.nivel === 'NIVEL_2'
                      ? 'bg-emerald-50 text-emerald-700'
                      : 'bg-teal-50 text-teal-700'
                  "
                >
                  {{ persona.nivel === 'NIVEL_2' ? 'Nivel 2' : 'Nivel 3' }}
                </span>
              </td>

              <td class="px-5 py-4">
                <span class="font-medium text-slate-700">
                  {{ persona.cargo }}
                </span>
              </td>

              <td class="px-5 py-4">
                <div class="flex items-center gap-2 text-slate-600">
                  <Mail :size="15" class="shrink-0 text-[#17665e]" />
                  <span>{{ persona.correo || '—' }}</span>
                </div>
                <p class="mt-1 text-xs text-slate-500">
                  {{ persona.celular || 'Sin celular' }}
                </p>
              </td>

              <td class="px-5 py-4 text-slate-600">
                {{ persona.situacion || '—' }}
              </td>

              <td class="px-5 py-4 text-slate-600">
                {{ persona.ranking ?? '—' }}
              </td>

              <td class="px-5 py-4 text-right">
                <button
                  type="button"
                  class="inline-flex items-center gap-2 rounded-lg border border-[#cfe0db] bg-white px-3 py-2 text-sm font-semibold text-[#17665e] transition hover:border-[#17665e] hover:bg-[#e0f2ee]"
                  :aria-label="`Ver detalle de ${nombreCompleto(persona)}`"
                  @click="verDetalle(persona.id)"
                >
                  <Eye :size="16" />
                  Ver detalle
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Sin resultados -->
      <div v-else class="flex flex-col items-center px-6 py-16 text-center">
        <div class="rounded-2xl bg-[#e0f2ee] p-4 text-[#17665e]">
          <Search :size="30" />
        </div>

        <h3 class="mt-4 font-bold text-[#123f3b]">No se encontraron registros</h3>

        <p class="mt-2 text-sm text-slate-500">Prueba con otro término o cambia los filtros.</p>

        <button
          type="button"
          class="mt-4 text-sm font-semibold text-[#17665e] hover:text-[#123f3b]"
          @click="limpiarFiltros"
        >
          Limpiar filtros
        </button>
      </div>

      <!-- Pie de tabla -->
      <div
        v-if="!cargando && !error && personalFiltrado.length"
        class="border-t border-[#e7efec] bg-[#fbfdfc] px-5 py-4"
      >
        <p class="text-xs text-slate-500">
          Mostrando {{ personalFiltrado.length }} registros. Los resultados corresponden al personal
          devuelto por el servidor.
        </p>
      </div>
    </section>

    <!-- Modal de importación -->
    <Teleport to="body">
      <Transition
        enter-active-class="duration-200 ease-out"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="duration-150 ease-in"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div
          v-if="modalImportacionAbierto"
          class="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-slate-950/60 p-4 backdrop-blur-sm"
          @click.self="cerrarModalImportacion"
        >
          <section
            role="dialog"
            aria-modal="true"
            aria-labelledby="tituloModalImportacion"
            class="my-auto w-full max-w-2xl overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white shadow-2xl"
          >
            <!-- Encabezado del modal -->
            <header
              class="flex items-start justify-between gap-4 border-b border-[#e7efec] px-6 py-5"
            >
              <div class="flex items-start gap-3">
                <div
                  class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-[#e0f2ee] text-[#17665e]"
                >
                  <FileSpreadsheet :size="23" />
                </div>

                <div>
                  <h2 id="tituloModalImportacion" class="text-lg font-bold text-[#123f3b]">
                    Agregar personal desde Excel
                  </h2>
                  <p class="mt-1 text-sm text-slate-500">
                    Importa o actualiza registros mediante un archivo Excel.
                  </p>
                </div>
              </div>

              <button
                type="button"
                aria-label="Cerrar ventana"
                :disabled="importando"
                class="rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700 disabled:opacity-40"
                @click="cerrarModalImportacion"
              >
                <X :size="21" />
              </button>
            </header>

            <!-- Formulario -->
            <form class="space-y-5 p-6" @submit.prevent="importarExcel">
              Get-ChildItem .\src\views -Directory
              <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <button
                  type="button"
                  class="flex items-center justify-center gap-2 rounded-xl border border-[#dbe9e5] bg-[#f5faf8] px-4 py-3 text-sm font-semibold text-[#17665e] transition hover:bg-[#e0f2ee]"
                  @click="descargarPlantillaNivel2"
                >
                  <FileSpreadsheet class="h-5 w-5" />
                  Descargar plantilla Nivel 2
                </button>

                <button
                  type="button"
                  class="flex items-center justify-center gap-2 rounded-xl border border-[#dbe9e5] bg-[#f5faf8] px-4 py-3 text-sm font-semibold text-[#17665e] transition hover:bg-[#e0f2ee]"
                  @click="descargarPlantillaNivel3"
                >
                  <FileSpreadsheet class="h-5 w-5" />
                  Descargar plantilla Nivel 3
                </button>
              </div>

              <div>
                <label
                  for="nivelImportacion"
                  class="mb-2 block text-sm font-semibold text-slate-700"
                >
                  Nivel de personal
                </label>
                <select
                  id="nivelImportacion"
                  v-model="nivelImportacion"
                  :disabled="importando"
                  class="w-full rounded-xl border border-[#dbe9e5] bg-[#fbfdfc] px-4 py-3 text-sm text-slate-700 outline-none transition focus:border-[#17665e] focus:ring-4 focus:ring-[#17665e]/10 disabled:opacity-60"
                >
                  <option value="NIVEL_2">Nivel 2</option>
                  <option value="NIVEL_3">Nivel 3</option>
                </select>

                <p class="mt-2 text-xs leading-5 text-slate-500">
                  Selecciona el nivel que corresponda a las columnas de tu archivo.
                </p>
              </div>

              <div>
                <label
                  for="archivoImportacion"
                  class="mb-2 block text-sm font-semibold text-slate-700"
                >
                  Archivo Excel
                </label>

                <div
                  class="rounded-xl border-2 border-dashed border-[#cfe0db] bg-[#fbfdfc] p-5 transition hover:border-[#17665e]"
                >
                  <div class="flex flex-col items-center text-center">
                    <div class="rounded-xl bg-[#e0f2ee] p-3 text-[#17665e]">
                      <Upload :size="25" />
                    </div>

                    <p class="mt-3 text-sm font-semibold text-[#123f3b]">
                      Selecciona tu archivo Excel
                    </p>

                    <p class="mt-1 text-xs text-slate-500">Formato admitido: .xlsx</p>

                    <input
                      id="archivoImportacion"
                      type="file"
                      accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                      :disabled="importando"
                      class="mt-4 block w-full max-w-sm cursor-pointer text-sm text-slate-600 file:mr-3 file:cursor-pointer file:rounded-lg file:border-0 file:bg-[#e0f2ee] file:px-4 file:py-2.5 file:font-semibold file:text-[#17665e] hover:file:bg-[#cce9e2] disabled:opacity-60"
                      @change="seleccionarArchivo"
                    />
                  </div>

                  <div
                    v-if="archivoImportacion"
                    class="mt-4 flex items-center gap-3 rounded-lg bg-emerald-50 p-3"
                  >
                    <FileSpreadsheet :size="22" class="shrink-0 text-emerald-700" />
                    <div class="min-w-0 flex-1">
                      <p class="truncate text-sm font-semibold text-emerald-800">
                        {{ archivoImportacion.name }}
                      </p>
                      <p class="text-xs text-emerald-700">Archivo seleccionado</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Error HTTP o validación local -->
              <div
                v-if="errorImportacion"
                role="alert"
                class="flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700"
              >
                <AlertCircle :size="20" class="mt-0.5 shrink-0" />
                <p class="wrap-break-word">{{ errorImportacion }}</p>
              </div>

              <!-- Resultado -->
              <div
                v-if="resultadoImportacion"
                class="rounded-xl border p-4"
                :class="
                  resultadoImportacion.valido && resultadoImportacion.filasConError === 0
                    ? 'border-emerald-200 bg-emerald-50'
                    : 'border-amber-200 bg-amber-50'
                "
              >
                <div class="flex items-start gap-2">
                  <CheckCircle2
                    v-if="resultadoImportacion.valido && resultadoImportacion.filasConError === 0"
                    :size="20"
                    class="mt-0.5 shrink-0 text-emerald-700"
                  />
                  <AlertCircle v-else :size="20" class="mt-0.5 shrink-0 text-amber-700" />

                  <div class="min-w-0 flex-1">
                    <h3
                      class="font-semibold"
                      :class="
                        resultadoImportacion.valido && resultadoImportacion.filasConError === 0
                          ? 'text-emerald-800'
                          : 'text-amber-800'
                      "
                    >
                      {{
                        resultadoImportacion.valido && resultadoImportacion.filasConError === 0
                          ? 'Importación completada'
                          : 'Se encontraron observaciones'
                      }}
                    </h3>

                    <p class="mt-1 wrap-break-word text-sm text-slate-600">
                      {{ resultadoImportacion.archivo }}
                    </p>
                  </div>
                </div>

                <div class="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
                  <div class="rounded-lg bg-white p-3">
                    <p class="text-xs text-slate-500">Filas totales</p>
                    <p class="mt-1 text-xl font-bold text-slate-800">
                      {{ resultadoImportacion.totalFilas }}
                    </p>
                  </div>

                  <div class="rounded-lg bg-white p-3">
                    <p class="text-xs text-slate-500">Filas válidas</p>
                    <p class="mt-1 text-xl font-bold text-emerald-700">
                      {{ resultadoImportacion.filasValidas }}
                    </p>
                  </div>

                  <div class="rounded-lg bg-white p-3">
                    <p class="text-xs text-slate-500">Filas con error</p>
                    <p class="mt-1 text-xl font-bold text-red-600">
                      {{ resultadoImportacion.filasConError }}
                    </p>
                  </div>

                  <div class="rounded-lg bg-white p-3">
                    <p class="text-xs text-slate-500">Registros nuevos</p>
                    <p class="mt-1 text-xl font-bold text-slate-800">
                      {{ resultadoImportacion.registrosNuevos }}
                    </p>
                  </div>

                  <div class="rounded-lg bg-white p-3">
                    <p class="text-xs text-slate-500">Actualizados</p>
                    <p class="mt-1 text-xl font-bold text-slate-800">
                      {{ resultadoImportacion.registrosExistentes }}
                    </p>
                  </div>
                </div>

                <div v-if="resultadoImportacion.errores?.length" class="mt-4">
                  <h4 class="mb-2 text-sm font-semibold text-red-700">Detalle de errores</h4>
                  <ul
                    class="max-h-40 list-inside list-disc space-y-1 overflow-y-auto text-sm text-red-700"
                  >
                    <li
                      v-for="(mensaje, indice) in resultadoImportacion.errores"
                      :key="`${indice}-${mensaje}`"
                      class="wrap-break-word"
                    >
                      {{ mensaje }}
                    </li>
                  </ul>
                </div>
              </div>

              <!-- Acciones -->
              <footer
                class="flex flex-col-reverse gap-3 border-t border-[#e7efec] pt-5 sm:flex-row sm:justify-end"
              >
                <button
                  type="button"
                  :disabled="importando"
                  class="inline-flex items-center justify-center gap-2 rounded-xl border border-[#dbe9e5] px-5 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                  @click="cerrarModalImportacion"
                >
                  {{ resultadoImportacion ? 'Cerrar' : 'Cancelar' }}
                </button>

                <button
                  type="submit"
                  :disabled="importando || !archivoImportacion"
                  class="inline-flex items-center justify-center gap-2 rounded-xl bg-[#17665e] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#123f3b] disabled:cursor-not-allowed disabled:opacity-50"
                >
                  <LoaderCircle v-if="importando" :size="18" class="animate-spin" />
                  <Upload v-else :size="18" />
                  {{
                    importando
                      ? 'Importando...'
                      : resultadoImportacion
                        ? 'Volver a importar'
                        : 'Importar archivo'
                  }}
                </button>
              </footer>
            </form>
          </section>
        </div>
      </Transition>
    </Teleport>
  </main>
</template>
