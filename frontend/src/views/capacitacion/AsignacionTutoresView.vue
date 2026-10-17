<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  Users,
  UserPlus,
  UserMinus,
  Search,
  RefreshCw,
  GraduationCap,
  DoorOpen,
  ClipboardList,
  AlertCircle,
} from 'lucide-vue-next'
import Swal from 'sweetalert2'

import { capacitacionService } from '@/services/capacitacionService'
import { asignacionTutorService } from '@/services/asignacionTutorService'
import { personalService } from '@/services/personalService'

import type { Capacitacion } from '@/types/capacitacion'
import type { Personal } from '@/types/personal'
import type { AsignacionTutor, AulaCapacitacion, FuncionTutor } from '@/types/asignacionTutor'

const capacitaciones = ref<Capacitacion[]>([])
const personal = ref<Personal[]>([])
const aulas = ref<AulaCapacitacion[]>([])
const asignaciones = ref<AsignacionTutor[]>([])

const capacitacionId = ref('')
const aulaCapacitacionId = ref('')
const personalId = ref('')
const funcion = ref<FuncionTutor>('RESPONSABLE')
const busqueda = ref('')

const cargando = ref(false)
const guardando = ref(false)
const error = ref('')

const personalDisponible = computed(() => {
  const personasAsignadas = new Set(
    asignaciones.value
      .filter((asignacion) => asignacion.activa)
      .map((asignacion) => asignacion.personalId),
  )

  return personal.value.filter(
    (persona) => persona.nivel === 'NIVEL_2' && !personasAsignadas.has(persona.id),
  )
})

const asignacionesFiltradas = computed(() => {
  const termino = busqueda.value.trim().toLowerCase()

  return asignaciones.value.filter((asignacion) => {
    const coincideAula =
      !aulaCapacitacionId.value || asignacion.aulaCapacitacionId === aulaCapacitacionId.value

    const codigoAula =
      aulas.value.find((aula) => aula.id === asignacion.aulaCapacitacionId)?.codigoAula ?? ''

    const texto = [
      asignacion.dni,
      asignacion.nombres,
      asignacion.apellidoPaterno,
      asignacion.apellidoMaterno,
      asignacion.cargo,
      asignacion.funcion,
      codigoAula,
    ]
      .join(' ')
      .toLowerCase()

    return coincideAula && texto.includes(termino)
  })
})

const aulaSeleccionada = computed(() =>
  aulas.value.find((aula) => aula.id === aulaCapacitacionId.value),
)

const personaSeleccionada = computed(() =>
  personal.value.find((persona) => persona.id === personalId.value),
)

const totalResponsables = computed(
  () =>
    asignacionesFiltradas.value.filter(
      (asignacion) => asignacion.activa && asignacion.funcion === 'RESPONSABLE',
    ).length,
)

const totalApoyos = computed(
  () =>
    asignacionesFiltradas.value.filter(
      (asignacion) => asignacion.activa && asignacion.funcion === 'APOYO',
    ).length,
)

function nombreCompleto(persona: Personal): string {
  return `${persona.apellidoPaterno} ${persona.apellidoMaterno}, ${persona.nombres}`
}

function nombreTutor(asignacion: AsignacionTutor): string {
  return `${asignacion.apellidoPaterno} ${asignacion.apellidoMaterno}, ${asignacion.nombres}`
}

function mostrarError(mensaje: string) {
  error.value = mensaje

  void Swal.fire({
    icon: 'error',
    title: 'No se pudo completar la operación',
    text: mensaje,
    confirmButtonColor: '#17665e',
  })
}

async function cargarDatos() {
  cargando.value = true
  error.value = ''

  try {
    const [listaCapacitaciones, listaPersonal] = await Promise.all([
      capacitacionService.listar(),
      personalService.listar(),
    ])

    capacitaciones.value = listaCapacitaciones
    personal.value = listaPersonal

    if (
      capacitacionId.value &&
      !capacitaciones.value.some((capacitacion) => capacitacion.id === capacitacionId.value)
    ) {
      capacitacionId.value = ''
    }

    if (!capacitacionId.value) {
      const primeraCapacitacion = capacitaciones.value.at(0)

      if (primeraCapacitacion) {
        capacitacionId.value = primeraCapacitacion.id
      }
    }
  } catch (e: unknown) {
    const mensaje =
      e instanceof Error
        ? e.message
        : 'Ocurrió un error al cargar las capacitaciones y el personal.'

    mostrarError(mensaje)
  } finally {
    cargando.value = false
  }
}

async function cargarDatosCapacitacion() {
  if (!capacitacionId.value) {
    aulas.value = []
    asignaciones.value = []
    aulaCapacitacionId.value = ''
    return
  }

  cargando.value = true
  error.value = ''

  try {
    const [listaAulas, listaAsignaciones] = await Promise.all([
      asignacionTutorService.listarAulas(capacitacionId.value),
      asignacionTutorService.listarTutores(capacitacionId.value),
    ])

    aulas.value = listaAulas
    asignaciones.value = listaAsignaciones

    if (
      aulaCapacitacionId.value &&
      !aulas.value.some((aula) => aula.id === aulaCapacitacionId.value)
    ) {
      aulaCapacitacionId.value = ''
    }
  } catch (e: unknown) {
    const mensaje =
      e instanceof Error ? e.message : 'No se pudieron cargar las aulas y las asignaciones.'

    mostrarError(mensaje)
  } finally {
    cargando.value = false
  }
}

async function asignarTutor() {
  if (!capacitacionId.value) {
    mostrarError('Selecciona una capacitación.')
    return
  }

  if (!aulaCapacitacionId.value) {
    mostrarError('Selecciona un aula de capacitación.')
    return
  }

  if (!personalId.value) {
    mostrarError('Selecciona al personal que deseas asignar.')
    return
  }

  const persona = personaSeleccionada.value
  const aula = aulaSeleccionada.value

  const confirmacion = await Swal.fire({
    icon: 'question',
    title: '¿Asignar tutor?',
    html: `
      <p>Se asignará el siguiente personal al aula seleccionada:</p>
      <p class="mt-3 font-semibold">${persona ? nombreCompleto(persona) : ''}</p>
      <p class="mt-1 text-sm">Función: ${funcion.value}</p>
      <p class="mt-1 text-sm">Aula: ${aula?.codigoAula ?? ''}</p>
    `,
    showCancelButton: true,
    confirmButtonText: 'Sí, asignar',
    cancelButtonText: 'Cancelar',
    confirmButtonColor: '#17665e',
  })

  if (!confirmacion.isConfirmed) return

  guardando.value = true

  try {
    await asignacionTutorService.asignarTutor(capacitacionId.value, aulaCapacitacionId.value, {
      personalId: personalId.value,
      funcion: funcion.value,
    })

    personalId.value = ''
    funcion.value = 'RESPONSABLE'

    await cargarDatosCapacitacion()

    await Swal.fire({
      icon: 'success',
      title: 'Tutor asignado',
      text: 'La asignación se registró correctamente.',
      confirmButtonColor: '#17665e',
    })
  } catch (e: unknown) {
    const mensaje = e instanceof Error ? e.message : 'No se pudo asignar el tutor.'

    mostrarError(mensaje)
  } finally {
    guardando.value = false
  }
}

async function quitarTutor(asignacion: AsignacionTutor) {
  const confirmacion = await Swal.fire({
    icon: 'warning',
    title: '¿Retirar esta asignación?',
    text: `Se retirará la asignación de ${nombreTutor(asignacion)}.`,
    showCancelButton: true,
    confirmButtonText: 'Sí, retirar',
    cancelButtonText: 'Cancelar',
    confirmButtonColor: '#b91c1c',
  })

  if (!confirmacion.isConfirmed) return

  guardando.value = true

  try {
    await asignacionTutorService.quitarTutor(asignacion.capacitacionId, asignacion.id)

    await cargarDatosCapacitacion()

    await Swal.fire({
      icon: 'success',
      title: 'Asignación retirada',
      text: 'La operación se realizó correctamente.',
      confirmButtonColor: '#17665e',
    })
  } catch (e: unknown) {
    const mensaje = e instanceof Error ? e.message : 'No se pudo retirar la asignación.'

    mostrarError(mensaje)
  } finally {
    guardando.value = false
  }
}

watch(capacitacionId, async () => {
  aulaCapacitacionId.value = ''
  await cargarDatosCapacitacion()
})

onMounted(cargarDatos)
</script>

<template>
  <main class="min-h-screen space-y-6 bg-[#edf5f3] p-4 text-[#123f3b] sm:p-6">
    <!-- Encabezado -->
    <section
      class="flex flex-col gap-4 rounded-2xl bg-[#17665e] p-6 text-white shadow-sm sm:flex-row sm:items-center sm:justify-between"
    >
      <div>
        <div class="mb-2 flex items-center gap-2 text-sm text-emerald-100">
          <GraduationCap :size="18" />
          <span>Gestión de capacitación</span>
        </div>

        <h1 class="text-2xl font-bold tracking-tight sm:text-3xl">Asignación de tutores</h1>

        <p class="mt-2 max-w-2xl text-sm text-emerald-50">
          Administra los responsables y apoyos de las aulas de capacitación.
        </p>
      </div>
    </section>

    <!-- Error -->
    <section
      v-if="error"
      class="flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700"
    >
      <AlertCircle :size="20" class="mt-0.5 shrink-0" />
      <p>{{ error }}</p>
    </section>

    <!-- Selección de capacitación -->
    <section class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
      <label class="mb-2 block text-sm font-semibold"> Capacitación </label>

      <select
        v-model="capacitacionId"
        class="w-full rounded-xl border border-[#dbe9e5] bg-white px-4 py-3 text-sm outline-none transition focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/10"
      >
        <option value="">Selecciona una capacitación</option>

        <option
          v-for="capacitacion in capacitaciones"
          :key="capacitacion.id"
          :value="capacitacion.id"
        >
          {{ capacitacion.nombre }} — {{ capacitacion.anio }}
        </option>
      </select>

      <p v-if="capacitaciones.length === 0 && !cargando" class="mt-3 text-sm text-slate-500">
        No hay capacitaciones registradas.
      </p>
    </section>

    <!-- Indicadores -->
    <section class="grid grid-cols-1 gap-4 sm:grid-cols-3">
      <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
        <div class="flex items-center justify-between">
          <p class="text-sm text-slate-500">Aulas asignadas</p>
          <DoorOpen :size="21" class="text-[#17665e]" />
        </div>
        <p class="mt-3 text-3xl font-bold">{{ aulas.length }}</p>
      </article>

      <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
        <div class="flex items-center justify-between">
          <p class="text-sm text-slate-500">Responsables</p>
          <Users :size="21" class="text-[#17665e]" />
        </div>
        <p class="mt-3 text-3xl font-bold">{{ totalResponsables }}</p>
      </article>

      <article class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm">
        <div class="flex items-center justify-between">
          <p class="text-sm text-slate-500">Apoyos</p>
          <ClipboardList :size="21" class="text-[#17665e]" />
        </div>
        <p class="mt-3 text-3xl font-bold">{{ totalApoyos }}</p>
      </article>
    </section>

    <!-- Formulario de asignación -->
    <section class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm sm:p-6">
      <div class="mb-5 flex items-center gap-3">
        <div class="rounded-xl bg-[#e0f2ee] p-3 text-[#17665e]">
          <UserPlus :size="22" />
        </div>

        <div>
          <h2 class="text-lg font-bold">Asignar personal como tutor</h2>
          <p class="mt-1 text-sm text-slate-500">
            Selecciona el aula, el personal y la función que desempeñará.
          </p>
        </div>
      </div>

      <form class="grid grid-cols-1 gap-4 lg:grid-cols-2" @submit.prevent="asignarTutor">
        <div>
          <label class="mb-2 block text-sm font-semibold"> Aula de capacitación </label>

          <select
            v-model="aulaCapacitacionId"
            required
            :disabled="!capacitacionId || cargando"
            class="w-full rounded-xl border border-[#dbe9e5] bg-white px-4 py-3 text-sm outline-none focus:border-[#17665e] disabled:bg-slate-50"
          >
            <option value="">Selecciona un aula</option>

            <option v-for="aula in aulas" :key="aula.id" :value="aula.id">
              {{ aula.codigoAula }} — {{ aula.tipoAula }} (Aforo: {{ aula.capacidad }})
            </option>
          </select>

          <p
            v-if="capacitacionId && aulas.length === 0 && !cargando"
            class="mt-2 text-xs text-amber-700"
          >
            Esta capacitación todavía no tiene aulas asignadas.
          </p>
        </div>

        <div>
          <label class="mb-2 block text-sm font-semibold"> Función del tutor </label>

          <select
            v-model="funcion"
            required
            class="w-full rounded-xl border border-[#dbe9e5] bg-white px-4 py-3 text-sm outline-none focus:border-[#17665e]"
          >
            <option value="RESPONSABLE">Responsable</option>
            <option value="APOYO">Apoyo</option>
          </select>
        </div>

        <div class="lg:col-span-2">
          <label class="mb-2 block text-sm font-semibold"> Personal de nivel 2 </label>

          <select
            v-model="personalId"
            required
            :disabled="cargando"
            class="w-full rounded-xl border border-[#dbe9e5] bg-white px-4 py-3 text-sm outline-none focus:border-[#17665e] disabled:bg-slate-50"
          >
            <option value="">Selecciona una persona</option>

            <option v-for="persona in personalDisponible" :key="persona.id" :value="persona.id">
              {{ persona.dni }} — {{ nombreCompleto(persona) }} — {{ persona.cargo }}
            </option>
          </select>

          <p
            v-if="personalDisponible.length === 0 && !cargando"
            class="mt-2 text-xs text-slate-500"
          >
            No hay personal de nivel 3 disponible para asignar.
          </p>
        </div>

        <div class="lg:col-span-2">
          <button
            type="submit"
            :disabled="
              guardando || cargando || !capacitacionId || !aulaCapacitacionId || !personalId
            "
            class="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-[#17665e] px-5 py-3 font-semibold text-white transition hover:bg-[#12544d] disabled:cursor-not-allowed disabled:opacity-50 sm:w-auto"
          >
            <UserPlus :size="18" />
            {{ guardando ? 'Procesando...' : 'Asignar tutor' }}
          </button>
        </div>
      </form>
    </section>

    <!-- Tabla de asignaciones -->
    <section class="overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white shadow-sm">
      <div
        class="flex flex-col gap-4 border-b border-[#dbe9e5] p-5 sm:flex-row sm:items-center sm:justify-between"
      >
        <div>
          <h2 class="text-lg font-bold">Tutores asignados</h2>
          <p class="mt-1 text-sm text-slate-500">
            {{ asignacionesFiltradas.length }} asignación(es) encontrada(s).
          </p>
        </div>

        <div class="flex flex-col gap-3 sm:flex-row">
          <select
            v-model="aulaCapacitacionId"
            class="rounded-xl border border-[#dbe9e5] bg-white px-3 py-2.5 text-sm outline-none focus:border-[#17665e]"
          >
            <option value="">Todas las aulas</option>

            <option v-for="aula in aulas" :key="aula.id" :value="aula.id">
              {{ aula.codigoAula }}
            </option>
          </select>

          <div class="relative">
            <Search :size="17" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />

            <input
              v-model="busqueda"
              type="search"
              placeholder="Buscar tutor..."
              class="w-full rounded-xl border border-[#dbe9e5] py-2.5 pl-9 pr-3 text-sm outline-none focus:border-[#17665e] sm:w-56"
            />
          </div>
        </div>
      </div>

      <div v-if="cargando" class="p-10 text-center text-sm text-slate-500">
        Cargando información...
      </div>

      <div v-else-if="asignacionesFiltradas.length === 0" class="p-10 text-center">
        <Users :size="36" class="mx-auto text-slate-300" />
        <p class="mt-3 font-semibold">No hay tutores para mostrar</p>
        <p class="mt-1 text-sm text-slate-500">
          Selecciona una capacitación o registra una nueva asignación.
        </p>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full min-w-225 text-left text-sm">
          <thead class="bg-[#f5faf8] text-xs uppercase tracking-wide text-slate-500">
            <tr>
              <th class="px-5 py-4 font-semibold">Personal</th>
              <th class="px-5 py-4 font-semibold">DNI</th>
              <th class="px-5 py-4 font-semibold">Cargo</th>
              <th class="px-5 py-4 font-semibold">Aula</th>
              <th class="px-5 py-4 font-semibold">Función</th>
              <th class="px-5 py-4 font-semibold">Estado</th>
              <th class="px-5 py-4 text-right font-semibold">Acciones</th>
            </tr>
          </thead>

          <tbody class="divide-y divide-[#e7efec]">
            <tr
              v-for="asignacion in asignacionesFiltradas"
              :key="asignacion.id"
              class="transition hover:bg-[#f8fbfa]"
            >
              <td class="px-5 py-4">
                <p class="font-semibold text-[#123f3b]">
                  {{ nombreTutor(asignacion) }}
                </p>
              </td>

              <td class="px-5 py-4 text-slate-600">
                {{ asignacion.dni }}
              </td>

              <td class="px-5 py-4 text-slate-600">
                {{ asignacion.cargo }}
              </td>

              <td class="px-5 py-4">
                <span
                  class="rounded-lg bg-[#e0f2ee] px-2.5 py-1.5 text-xs font-semibold text-[#17665e]"
                >
                  {{
                    aulas.find((aula) => aula.id === asignacion.aulaCapacitacionId)?.codigoAula ??
                    'Aula'
                  }}
                </span>
              </td>

              <td class="px-5 py-4">
                <span
                  class="rounded-full px-3 py-1 text-xs font-semibold"
                  :class="
                    asignacion.funcion === 'RESPONSABLE'
                      ? 'bg-[#dff4e8] text-[#166534]'
                      : 'bg-blue-50 text-blue-700'
                  "
                >
                  {{ asignacion.funcion === 'RESPONSABLE' ? 'Responsable' : 'Apoyo' }}
                </span>
              </td>

              <td class="px-5 py-4">
                <span
                  class="rounded-full px-3 py-1 text-xs font-semibold"
                  :class="
                    asignacion.activa
                      ? 'bg-emerald-50 text-emerald-700'
                      : 'bg-slate-100 text-slate-500'
                  "
                >
                  {{ asignacion.activa ? 'Activa' : 'Inactiva' }}
                </span>
              </td>

              <td class="px-5 py-4 text-right">
                <button
                  v-if="asignacion.activa"
                  type="button"
                  :disabled="guardando"
                  title="Retirar asignación"
                  class="inline-flex items-center gap-2 rounded-lg border border-red-200 px-3 py-2 text-xs font-semibold text-red-700 transition hover:bg-red-50 disabled:opacity-50"
                  @click="quitarTutor(asignacion)"
                >
                  <UserMinus :size="15" />
                  Retirar
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </main>
</template>
