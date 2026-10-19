```vue
<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  Users,
  Search,
  RefreshCw,
  GraduationCap,
  School,
  UserCheck,
  UserRound,
  FileSpreadsheet,
  Filter,
  X,
} from 'lucide-vue-next'
import * as XLSX from 'xlsx'
import Swal from 'sweetalert2'

import { capacitacionService } from '@/services/capacitacionService'
import { aulaCapacitacionService } from '@/services/aulaCapacitacionService'
import { asignacionParticipanteService } from '@/services/asignacionParticipanteService'
import { asignacionTutorService } from '@/services/asignacionTutorService'

import type { Capacitacion } from '@/types/capacitacion'
import type { AulaCapacitacion } from '@/types/aula'
import type { AsignacionTutor } from '@/types/asignacionTutor'
import type { AsignacionParticipanteResponse } from '@/types/asignacionParticipante'

type TipoFiltro = 'TODOS' | 'APLICADOR' | 'ORIENTADOR'

interface ParticipanteVista extends AsignacionParticipanteResponse {
  tutorResponsable: string
  tutorApoyo: string
}

interface ResumenAula {
  codigoAula: string
  tipoAula: string
  total: number
  aplicadores: number
  orientadores: number
}

const capacitaciones = ref<Capacitacion[]>([])
const aulas = ref<AulaCapacitacion[]>([])
const participantes = ref<AsignacionParticipanteResponse[]>([])
const tutores = ref<AsignacionTutor[]>([])

const capacitacionId = ref('')
const tipoFiltro = ref<TipoFiltro>('TODOS')
const aulaFiltro = ref('TODAS')
const busqueda = ref('')

const cargandoInicial = ref(true)
const cargandoDatos = ref(false)
const exportando = ref(false)
const errorCarga = ref('')

const capacitacionSeleccionada = computed(() =>
  capacitaciones.value.find((item) => item.id === capacitacionId.value),
)

const aulasActivas = computed(() => aulas.value.filter((aula) => aula.activa))

const aulasFiltradas = computed(() => {
  let resultado = aulasActivas.value

  if (tipoFiltro.value !== 'TODOS') {
    resultado = resultado.filter((aula) => aula.tipoAula.toUpperCase() === tipoFiltro.value)
  }

  return [...resultado].sort((a, b) =>
    a.codigoAula.localeCompare(b.codigoAula, 'es', {
      numeric: true,
      sensitivity: 'base',
    }),
  )
})

const opcionesAula = computed(() => {
  const codigos = new Set(aulasFiltradas.value.map((aula) => aula.id))

  return aulasFiltradas.value.filter((aula) => codigos.has(aula.id))
})

function nombreCompleto(persona: {
  nombres?: string | null
  apellidoPaterno?: string | null
  apellidoMaterno?: string | null
}): string {
  return [persona.apellidoPaterno, persona.apellidoMaterno, persona.nombres]
    .filter(Boolean)
    .join(' ')
    .trim()
}

function normalizarTexto(valor: unknown): string {
  return String(valor ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase('es')
    .trim()
}

function obtenerTutor(aulaCapacitacionId: string, funcion: 'RESPONSABLE' | 'APOYO'): string {
  const tutor = tutores.value.find(
    (item) =>
      item.aulaCapacitacionId === aulaCapacitacionId && item.funcion === funcion && item.activa,
  )

  return tutor ? nombreCompleto(tutor) : 'Sin asignar'
}

const participantesVista = computed<ParticipanteVista[]>(() =>
  participantes.value.map((participante) => ({
    ...participante,
    tutorResponsable: obtenerTutor(participante.aulaCapacitacionId, 'RESPONSABLE'),
    tutorApoyo: obtenerTutor(participante.aulaCapacitacionId, 'APOYO'),
  })),
)

const participantesFiltrados = computed(() => {
  const termino = normalizarTexto(busqueda.value)

  return participantesVista.value
    .filter((participante) => {
      if (
        tipoFiltro.value !== 'TODOS' &&
        participante.tipoAula.toUpperCase() !== tipoFiltro.value
      ) {
        return false
      }

      if (aulaFiltro.value !== 'TODAS' && participante.aulaCapacitacionId !== aulaFiltro.value) {
        return false
      }

      if (!termino) {
        return true
      }

      const texto = normalizarTexto(
        [
          participante.dni,
          participante.apellidoPaterno,
          participante.apellidoMaterno,
          participante.nombres,
          participante.cargo,
          participante.funcion,
          participante.situacion,
          participante.codigoAula,
          participante.tutorResponsable,
          participante.tutorApoyo,
        ].join(' '),
      )

      return texto.includes(termino)
    })
    .sort((a, b) => {
      const aulaComparacion = a.codigoAula.localeCompare(b.codigoAula, 'es', {
        numeric: true,
        sensitivity: 'base',
      })

      if (aulaComparacion !== 0) {
        return aulaComparacion
      }

      const apellidoComparacion = (a.apellidoPaterno ?? '').localeCompare(
        b.apellidoPaterno ?? '',
        'es',
        {
          sensitivity: 'base',
        },
      )

      if (apellidoComparacion !== 0) {
        return apellidoComparacion
      }

      const maternoComparacion = (a.apellidoMaterno ?? '').localeCompare(
        b.apellidoMaterno ?? '',
        'es',
        {
          sensitivity: 'base',
        },
      )

      if (maternoComparacion !== 0) {
        return maternoComparacion
      }

      return (a.nombres ?? '').localeCompare(b.nombres ?? '', 'es', { sensitivity: 'base' })
    })
})

const totalParticipantes = computed(() => participantesFiltrados.value.length)

const totalAulas = computed(() => {
  const ids = new Set(
    participantesFiltrados.value.map((participante) => participante.aulaCapacitacionId),
  )

  return ids.size
})

const totalAplicadores = computed(
  () =>
    participantesFiltrados.value.filter(
      (participante) => participante.tipoAula.toUpperCase() === 'APLICADOR',
    ).length,
)

const totalOrientadores = computed(
  () =>
    participantesFiltrados.value.filter(
      (participante) => participante.tipoAula.toUpperCase() === 'ORIENTADOR',
    ).length,
)

const resumenAulas = computed<ResumenAula[]>(() => {
  const agrupados = new Map<string, ResumenAula>()

  for (const participante of participantesFiltrados.value) {
    const clave = participante.aulaCapacitacionId

    if (!agrupados.has(clave)) {
      agrupados.set(clave, {
        codigoAula: participante.codigoAula,
        tipoAula: participante.tipoAula,
        total: 0,
        aplicadores: 0,
        orientadores: 0,
      })
    }

    const resumen = agrupados.get(clave)

    if (!resumen) {
      continue
    }

    resumen.total += 1

    if (participante.tipoAula.toUpperCase() === 'APLICADOR') {
      resumen.aplicadores += 1
    }

    if (participante.tipoAula.toUpperCase() === 'ORIENTADOR') {
      resumen.orientadores += 1
    }
  }

  return [...agrupados.values()].sort((a, b) =>
    a.codigoAula.localeCompare(b.codigoAula, 'es', {
      numeric: true,
      sensitivity: 'base',
    }),
  )
})

function limpiarFiltros(): void {
  tipoFiltro.value = 'TODOS'
  aulaFiltro.value = 'TODAS'
  busqueda.value = ''
}

function cambiarTipo(): void {
  aulaFiltro.value = 'TODAS'
}

async function cargarCapacitaciones(): Promise<void> {
  try {
    const respuesta = await capacitacionService.listar()
    capacitaciones.value = respuesta

    if (capacitaciones.value.length > 0 && !capacitacionId.value) {
      const enCurso = capacitaciones.value.find((item) => item.estado === 'EN_CURSO')

      const primeraCapacitacion = capacitaciones.value[0]
      const seleccionada = enCurso ?? primeraCapacitacion

      if (seleccionada) {
        capacitacionId.value = seleccionada.id
      }
    }
  } catch (error) {
    console.error('Error al cargar las capacitaciones:', error)
    errorCarga.value = 'No se pudieron cargar las capacitaciones.'
  }
}

async function cargarDatos(): Promise<void> {
  if (!capacitacionId.value) {
    aulas.value = []
    participantes.value = []
    tutores.value = []
    return
  }

  cargandoDatos.value = true
  errorCarga.value = ''

  try {
    const [listaAulas, listaParticipantes, listaTutores] = await Promise.all([
      aulaCapacitacionService.listar(capacitacionId.value),
      asignacionParticipanteService.listar(capacitacionId.value),
      asignacionTutorService.listarTutores(capacitacionId.value),
    ])

    aulas.value = listaAulas
    participantes.value = listaParticipantes
    tutores.value = listaTutores
  } catch (error) {
    console.error('Error al cargar los participantes:', error)
    errorCarga.value = 'No se pudo cargar la información. Verifica la conexión con el servidor.'

    await Swal.fire({
      icon: 'error',
      title: 'Error al cargar los datos',
      text: errorCarga.value,
      confirmButtonColor: '#17665e',
    })
  } finally {
    cargandoDatos.value = false
  }
}

async function exportarExcel(): Promise<void> {
  if (participantesFiltrados.value.length === 0) {
    await Swal.fire({
      icon: 'info',
      title: 'Sin datos para exportar',
      text: 'No hay participantes que coincidan con los filtros seleccionados.',
      confirmButtonColor: '#17665e',
    })

    return
  }

  exportando.value = true

  try {
    const filasParticipantes = participantesFiltrados.value.map((participante, indice) => ({
      N: indice + 1,
      Aula: participante.codigoAula,
      'Tipo de aula': participante.tipoAula,
      DNI: participante.dni,
      'Apellido paterno': participante.apellidoPaterno,
      'Apellido materno': participante.apellidoMaterno,
      Nombres: participante.nombres,
      Cargo: participante.cargo,
      Función: participante.funcion,
      Situación: participante.situacion ?? '',
      Ranking: participante.ranking ?? '',
      'Tutor responsable': participante.tutorResponsable,
      'Tutor de apoyo': participante.tutorApoyo,
    }))

    const filasResumen = resumenAulas.value.map((aula) => ({
      Aula: aula.codigoAula,
      'Tipo de aula': aula.tipoAula,
      'Total de participantes': aula.total,
      Aplicadores: aula.aplicadores,
      Orientadores: aula.orientadores,
    }))

    const libro = XLSX.utils.book_new()

    const hojaParticipantes = XLSX.utils.json_to_sheet(filasParticipantes)

    hojaParticipantes['!cols'] = [
      { wch: 7 },
      { wch: 14 },
      { wch: 18 },
      { wch: 14 },
      { wch: 22 },
      { wch: 22 },
      { wch: 28 },
      { wch: 22 },
      { wch: 18 },
      { wch: 18 },
      { wch: 12 },
      { wch: 30 },
      { wch: 30 },
    ]

    XLSX.utils.book_append_sheet(libro, hojaParticipantes, 'Participantes por aula')

    const hojaResumen = XLSX.utils.json_to_sheet(filasResumen)

    hojaResumen['!cols'] = [{ wch: 16 }, { wch: 18 }, { wch: 24 }, { wch: 16 }, { wch: 16 }]

    XLSX.utils.book_append_sheet(libro, hojaResumen, 'Resumen por aula')

    const nombreArchivo = `eda_participantes_${new Date().toISOString().slice(0, 10)}.xlsx`

    XLSX.writeFile(libro, nombreArchivo)

    await Swal.fire({
      icon: 'success',
      title: 'Exportación completada',
      text: 'El archivo Excel se descargó correctamente.',
      confirmButtonColor: '#17665e',
    })
  } catch (error) {
    console.error('Error al exportar a Excel:', error)

    await Swal.fire({
      icon: 'error',
      title: 'Error al exportar',
      text: 'No fue posible generar el archivo Excel.',
      confirmButtonColor: '#17665e',
    })
  } finally {
    exportando.value = false
  }
}

watch(capacitacionId, async () => {
  if (cargandoInicial.value) {
    return
  }

  limpiarFiltros()
  await cargarDatos()
})

onMounted(async () => {
  cargandoInicial.value = true

  try {
    await cargarCapacitaciones()

    if (capacitacionId.value) {
      await cargarDatos()
    }
  } finally {
    cargandoInicial.value = false
  }
})
</script>

<template>
  <main class="min-h-screen bg-[#edf5f3] p-4 md:p-6 xl:p-8">
    <div class="mx-auto w-full max-w-[1600px] space-y-6">
      <section
        class="relative overflow-hidden rounded-3xl bg-linear-to-br from-[#123f3b] via-[#17665e] to-[#218879] p-6 text-white shadow-lg shadow-[#123f3b]/10 md:flex md:items-center md:justify-between md:gap-5 md:p-8"
      >
        <div
          class="pointer-events-none absolute -right-12 -top-16 h-56 w-56 rounded-full border-28 border-white/5"
        ></div>

        <div class="relative z-10 flex items-start gap-4">
          <div
            class="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl border border-white/15 bg-white/10 text-white"
          >
            <Users :size="26" />
          </div>

          <div>
            <h1 class="text-2xl font-bold sm:text-3xl">Participantes</h1>

            <p class="mt-2 max-w-2xl text-sm leading-6 text-[#d0e8e3]">
              Consulta de participantes, aulas y tutores asignados.
            </p>
          </div>
        </div>

        <div class="relative z-10 mt-5 flex flex-wrap gap-2 md:mt-0">
          <button
            type="button"
            class="inline-flex items-center justify-center gap-2 rounded-xl bg-white px-4 py-2.5 text-sm font-semibold text-[#17665e] transition hover:bg-[#e0f2ee] disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="exportando || cargandoDatos || participantesFiltrados.length === 0"
            @click="exportarExcel"
          >
            <FileSpreadsheet :size="17" />
            {{ exportando ? 'Exportando...' : 'Exportar Excel' }}
          </button>
        </div>
      </section>

      <!-- Selector de capacitación -->
      <section class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
        <label for="capacitacion" class="mb-2 block text-sm font-semibold text-[#123f3b]">
          Capacitación
        </label>

        <select
          id="capacitacion"
          v-model="capacitacionId"
          class="w-full rounded-xl border border-[#dbe9e5] bg-white px-4 py-3 text-sm text-[#123f3b] outline-none transition focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10 md:max-w-xl"
          :disabled="cargandoInicial || capacitaciones.length === 0"
        >
          <option value="" disabled>Selecciona una capacitación</option>

          <option
            v-for="capacitacion in capacitaciones"
            :key="capacitacion.id"
            :value="capacitacion.id"
          >
            {{ capacitacion.nombre }} — {{ capacitacion.anio }}
          </option>
        </select>

        <p v-if="capacitacionSeleccionada" class="mt-2 text-xs text-slate-500">
          Estado:
          <span class="font-semibold text-[#17665e]">
            {{ capacitacionSeleccionada.estado }}
          </span>
        </p>
      </section>

      <!-- Indicadores -->
      <section class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Participantes</p>
              <p class="mt-2 text-3xl font-bold text-[#123f3b]">
                {{ totalParticipantes }}
              </p>
            </div>

            <div
              class="flex h-11 w-11 items-center justify-center rounded-xl bg-[#e0f2ee] text-[#17665e]"
            >
              <Users :size="23" />
            </div>
          </div>
        </article>

        <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Aulas con participantes</p>
              <p class="mt-2 text-3xl font-bold text-[#123f3b]">
                {{ totalAulas }}
              </p>
            </div>

            <div
              class="flex h-11 w-11 items-center justify-center rounded-xl bg-[#e0f2ee] text-[#17665e]"
            >
              <School :size="23" />
            </div>
          </div>
        </article>

        <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Aulas de aplicadores</p>
              <p class="mt-2 text-3xl font-bold text-[#123f3b]">
                {{ totalAplicadores }}
              </p>
            </div>

            <div
              class="flex h-11 w-11 items-center justify-center rounded-xl bg-amber-50 text-amber-700"
            >
              <GraduationCap :size="23" />
            </div>
          </div>
        </article>

        <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-500">Aulas de orientadores</p>
              <p class="mt-2 text-3xl font-bold text-[#123f3b]">
                {{ totalOrientadores }}
              </p>
            </div>

            <div
              class="flex h-11 w-11 items-center justify-center rounded-xl bg-sky-50 text-sky-700"
            >
              <UserCheck :size="23" />
            </div>
          </div>
        </article>
      </section>

      <!-- Filtros -->
      <section class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
        <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
          <div class="flex items-center gap-2">
            <Filter :size="19" class="text-[#17665e]" />
            <h2 class="font-bold text-[#123f3b]">Filtros de búsqueda</h2>
          </div>

          <button
            type="button"
            class="text-sm font-semibold text-[#17665e] hover:underline"
            @click="limpiarFiltros"
          >
            Limpiar filtros
          </button>
        </div>

        <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
          <div>
            <label for="tipoAula" class="mb-2 block text-sm font-medium text-slate-600">
              Tipo de aula
            </label>

            <select
              id="tipoAula"
              v-model="tipoFiltro"
              class="w-full rounded-xl border border-[#dbe9e5] bg-white px-3 py-2.5 text-sm outline-none focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
              @change="cambiarTipo"
            >
              <option value="TODOS">Todos los tipos</option>
              <option value="APLICADOR">Aplicador</option>
              <option value="ORIENTADOR">Orientador</option>
            </select>
          </div>

          <div>
            <label for="aulaFiltro" class="mb-2 block text-sm font-medium text-slate-600">
              Aula
            </label>

            <select
              id="aulaFiltro"
              v-model="aulaFiltro"
              class="w-full rounded-xl border border-[#dbe9e5] bg-white px-3 py-2.5 text-sm outline-none focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
            >
              <option value="TODAS">Todas las aulas</option>

              <option v-for="aula in opcionesAula" :key="aula.id" :value="aula.id">
                {{ aula.codigoAula }} — {{ aula.tipoAula }}
              </option>
            </select>
          </div>

          <div>
            <label for="busqueda" class="mb-2 block text-sm font-medium text-slate-600">
              Buscar participante
            </label>

            <div class="relative">
              <Search :size="18" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />

              <input
                id="busqueda"
                v-model="busqueda"
                type="search"
                placeholder="DNI, apellidos, nombres o aula..."
                class="w-full rounded-xl border border-[#dbe9e5] bg-white py-2.5 pl-10 pr-10 text-sm outline-none focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
              />

              <button
                v-if="busqueda"
                type="button"
                aria-label="Borrar búsqueda"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-[#17665e]"
                @click="busqueda = ''"
              >
                <X :size="16" />
              </button>
            </div>
          </div>
        </div>

        <p class="mt-4 text-xs text-slate-500">
          Se encontraron
          <span class="font-bold text-[#17665e]">
            {{ participantesFiltrados.length }}
          </span>
          participantes con los filtros actuales.
        </p>
      </section>

      <!-- Error -->
      <section
        v-if="errorCarga"
        class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700"
      >
        {{ errorCarga }}
      </section>

      <!-- Tabla -->
      <section class="overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white shadow-sm">
        <div
          class="flex flex-wrap items-center justify-between gap-3 border-b border-[#dbe9e5] p-5"
        >
          <div>
            <h2 class="font-bold text-[#123f3b]">Relación de participantes</h2>
            <p class="mt-1 text-sm text-slate-500">Ordenados por aula y apellidos.</p>
          </div>

          <span class="rounded-full bg-[#e0f2ee] px-3 py-1 text-xs font-semibold text-[#17665e]">
            {{ participantesFiltrados.length }} registros
          </span>
        </div>

        <div
          v-if="cargandoInicial || cargandoDatos"
          class="flex flex-col items-center justify-center gap-3 px-4 py-16 text-center"
        >
          <RefreshCw :size="30" class="animate-spin text-[#17665e]" />
          <p class="text-sm text-slate-500">Cargando información de participantes...</p>
        </div>

        <div
          v-else-if="!capacitacionId"
          class="flex flex-col items-center justify-center px-4 py-16 text-center"
        >
          <School :size="38" class="mb-3 text-slate-300" />
          <h3 class="font-semibold text-[#123f3b]">No hay capacitaciones disponibles</h3>
          <p class="mt-1 text-sm text-slate-500">
            Registra o habilita una capacitación para consultar participantes.
          </p>
        </div>

        <div
          v-else-if="participantesFiltrados.length === 0"
          class="flex flex-col items-center justify-center px-4 py-16 text-center"
        >
          <Users :size="38" class="mb-3 text-slate-300" />
          <h3 class="font-semibold text-[#123f3b]">No se encontraron participantes</h3>
          <p class="mt-1 max-w-md text-sm text-slate-500">
            No hay resultados para los filtros seleccionados. Puedes limpiar los filtros o
            seleccionar otra capacitación.
          </p>

          <button
            type="button"
            class="mt-4 rounded-xl bg-[#17665e] px-4 py-2 text-sm font-semibold text-white hover:bg-[#12534d]"
            @click="limpiarFiltros"
          >
            Limpiar filtros
          </button>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full min-w-312.5 border-collapse text-left">
            <thead>
              <tr class="bg-[#f3f8f6] text-xs uppercase tracking-wide text-[#456b66]">
                <th class="px-4 py-4 font-bold">N.º</th>
                <th class="px-4 py-4 font-bold">Aula</th>
                <th class="px-4 py-4 font-bold">DNI</th>
                <th class="px-4 py-4 font-bold">Apellidos y nombres</th>
                <th class="px-4 py-4 font-bold">Cargo</th>
                <th class="px-4 py-4 font-bold">Función</th>
                <th class="px-4 py-4 font-bold">Situación</th>
                <th class="px-4 py-4 font-bold">Tutor responsable</th>
                <th class="px-4 py-4 font-bold">Tutor de apoyo</th>
              </tr>
            </thead>

            <tbody class="divide-y divide-[#edf3f1]">
              <tr
                v-for="(participante, indice) in participantesFiltrados"
                :key="participante.id"
                class="transition hover:bg-[#f7fbfa]"
              >
                <td class="whitespace-nowrap px-4 py-4 text-sm text-slate-500">
                  {{ indice + 1 }}
                </td>

                <td class="whitespace-nowrap px-4 py-4">
                  <div class="font-semibold text-[#123f3b]">
                    {{ participante.codigoAula }}
                  </div>
                  <span
                    class="mt-1 inline-flex rounded-full px-2 py-1 text-xs font-semibold"
                    :class="
                      participante.tipoAula.toUpperCase() === 'APLICADOR'
                        ? 'bg-amber-50 text-amber-700'
                        : 'bg-sky-50 text-sky-700'
                    "
                  >
                    {{ participante.tipoAula }}
                  </span>
                </td>

                <td class="whitespace-nowrap px-4 py-4 font-mono text-sm text-slate-600">
                  {{ participante.dni }}
                </td>

                <td class="px-4 py-4">
                  <div class="min-w-52.5 font-semibold text-[#123f3b]">
                    {{ nombreCompleto(participante) }}
                  </div>
                </td>

                <td class="px-4 py-4 text-sm text-slate-600">
                  {{ participante.cargo || '—' }}
                </td>

                <td class="px-4 py-4 text-sm text-slate-600">
                  {{ participante.funcion || '—' }}
                </td>

                <td class="px-4 py-4 text-sm text-slate-600">
                  {{ participante.situacion || '—' }}
                </td>

                <td class="px-4 py-4">
                  <div class="flex min-w-47.5 items-center gap-2">
                    <UserCheck :size="17" class="shrink-0 text-[#17665e]" />
                    <span class="text-sm text-slate-600">
                      {{ participante.tutorResponsable }}
                    </span>
                  </div>
                </td>

                <td class="px-4 py-4">
                  <div class="flex min-w-47.5 items-center gap-2">
                    <UserRound :size="17" class="shrink-0 text-slate-400" />
                    <span class="text-sm text-slate-600">
                      {{ participante.tutorApoyo }}
                    </span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div
          v-if="!cargandoInicial && !cargandoDatos && participantesFiltrados.length > 0"
          class="border-t border-[#dbe9e5] bg-[#f8fbfa] px-5 py-3 text-xs text-slate-500"
        >
          Mostrando {{ participantesFiltrados.length }} participantes. La información es de
          consulta; las asignaciones se gestionan desde su módulo correspondiente.
        </div>
      </section>

      <!-- Resumen por aula -->
      <section
        v-if="!cargandoDatos && resumenAulas.length > 0"
        class="overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white shadow-sm"
      >
        <div class="border-b border-[#dbe9e5] p-5">
          <div class="flex items-center gap-2">
            <School :size="20" class="text-[#17665e]" />
            <h2 class="font-bold text-[#123f3b]">Resumen por aula</h2>
          </div>

          <p class="mt-1 text-sm text-slate-500">
            Cantidad de participantes agrupada por aula según los filtros actuales.
          </p>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full min-w-150 text-left">
            <thead>
              <tr class="bg-[#f3f8f6] text-xs uppercase tracking-wide text-[#456b66]">
                <th class="px-5 py-3 font-bold">Aula</th>
                <th class="px-5 py-3 font-bold">Tipo</th>
                <th class="px-5 py-3 text-right font-bold">Participantes</th>
              </tr>
            </thead>

            <tbody class="divide-y divide-[#edf3f1]">
              <tr v-for="aula in resumenAulas" :key="aula.codigoAula" class="hover:bg-[#f7fbfa]">
                <td class="px-5 py-3 font-semibold text-[#123f3b]">
                  {{ aula.codigoAula }}
                </td>

                <td class="px-5 py-3 text-sm text-slate-600">
                  {{ aula.tipoAula }}
                </td>

                <td class="px-5 py-3 text-right">
                  <span
                    class="inline-flex min-w-10 justify-center rounded-full bg-[#e0f2ee] px-3 py-1 text-sm font-bold text-[#17665e]"
                  >
                    {{ aula.total }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </main>
</template>
```
