<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  AlertCircle,
  CheckCircle2,
  ClipboardCheck,
  DoorOpen,
  KeyRound,
  LoaderCircle,
  Search,
  UserRound,
  Users,
  X,
} from 'lucide-vue-next'
import Swal from 'sweetalert2'

import { api } from '@/services/api'
import { capacitacionService } from '@/services/capacitacionService'
import { asignacionTutorService } from '@/services/asignacionTutorService'
import { asignacionParticipanteService } from '@/services/asignacionParticipanteService'

import type { Capacitacion } from '@/types/capacitacion'
import type { AulaCapacitacion } from '@/types/asignacionTutor'
import type { Personal } from '@/types/personal'
import type { AsignacionParticipanteResponse } from '@/types/asignacionParticipante'

type TipoAula = 'APLICADOR' | 'ORIENTADOR'

const capacitaciones = ref<Capacitacion[]>([])
const capacitacionSeleccionada = ref<Capacitacion | null>(null)

const aulas = ref<AulaCapacitacion[]>([])
const participante = ref<Personal | null>(null)
const aulaSeleccionada = ref<AulaCapacitacion | null>(null)

const asignaciones = ref<AsignacionParticipanteResponse[]>([])

const dniIngresado = ref('')
const codigoReserva = ref('')
const tipoAulaReserva = ref<TipoAula>('APLICADOR')

const cargandoCapacitaciones = ref(false)
const cargandoAulas = ref(false)
const buscandoParticipante = ref(false)
const registrandoAsignacion = ref(false)
const cargandoAsignaciones = ref(false)

const errorCapacitaciones = ref('')
const errorAulas = ref('')
const mensajeBusqueda = ref('')

const esReserva = computed(() => {
  const cargo = participante.value?.cargo?.trim().toUpperCase() ?? ''
  const situacion = participante.value?.situacion?.trim().toUpperCase() ?? ''

  return cargo === 'RESERVA' || situacion === 'RESERVA'
})

const tipoAulaRequerido = computed<TipoAula | null>(() => {
  const persona = participante.value

  if (!persona || esReserva.value) {
    return null
  }

  const cargo = persona.cargo.trim().toUpperCase()

  if (cargo === 'APLICADOR') {
    return 'APLICADOR'
  }

  if (cargo === 'ORIENTADOR') {
    return 'ORIENTADOR'
  }

  return null
})

const aulasTipo = computed<AulaCapacitacion[]>(() => {
  const tipo = esReserva.value ? tipoAulaReserva.value : tipoAulaRequerido.value

  if (!tipo) {
    return []
  }

  return aulas.value.filter((aula) => {
    return aula.activa && aula.tipoAula.trim().toUpperCase() === tipo
  })
})

const participanteTieneCargoValido = computed(() => {
  if (!participante.value) {
    return false
  }

  return esReserva.value || tipoAulaRequerido.value !== null
})

const puedeContinuar = computed(() => {
  if (!participante.value || !capacitacionSeleccionada.value) {
    return false
  }

  if (!participanteTieneCargoValido.value) {
    return false
  }

  if (esReserva.value && !codigoReserva.value.trim()) {
    return false
  }

  return aulaSeleccionada.value !== null
})

function normalizarTexto(valor: string | null | undefined): string {
  return (valor ?? '').trim().toUpperCase()
}

function nombreCompleto(persona: Personal): string {
  return [persona.apellidoPaterno, persona.apellidoMaterno, persona.nombres]
    .filter(Boolean)
    .join(' ')
}

function limpiarBusqueda(): void {
  participante.value = null
  aulaSeleccionada.value = null
  codigoReserva.value = ''
  tipoAulaReserva.value = 'APLICADOR'
  mensajeBusqueda.value = ''
}

function limpiarTodo(): void {
  dniIngresado.value = ''
  limpiarBusqueda()
}

async function cargarCapacitaciones(): Promise<void> {
  cargandoCapacitaciones.value = true
  errorCapacitaciones.value = ''

  try {
    const respuesta = await capacitacionService.listar()

    capacitaciones.value = respuesta

    const enCurso = capacitaciones.value.find((capacitacion) => capacitacion.estado === 'EN_CURSO')

    const primera = capacitaciones.value[0]

    capacitacionSeleccionada.value = enCurso ?? primera ?? null

    if (!capacitacionSeleccionada.value) {
      aulas.value = []
      asignaciones.value = []
      errorCapacitaciones.value = 'No hay capacitaciones registradas.'
      return
    }

    await Promise.all([cargarAulas(), cargarAsignaciones()])
  } catch (error) {
    console.error('Error al cargar capacitaciones:', error)

    errorCapacitaciones.value = 'No se pudieron cargar las capacitaciones.'
  } finally {
    cargandoCapacitaciones.value = false
  }
}

async function cargarAulas(): Promise<void> {
  const capacitacion = capacitacionSeleccionada.value

  if (!capacitacion) {
    aulas.value = []
    aulaSeleccionada.value = null
    errorAulas.value = 'Selecciona una capacitación.'
    return
  }

  cargandoAulas.value = true
  errorAulas.value = ''

  try {
    aulas.value = await asignacionTutorService.listarAulas(capacitacion.id)

    aulaSeleccionada.value = null
  } catch (error) {
    console.error('Error al cargar aulas:', error)

    aulas.value = []
    aulaSeleccionada.value = null
    errorAulas.value = 'No se pudieron cargar las aulas de esta capacitación.'
  } finally {
    cargandoAulas.value = false
  }
}

async function cargarAsignaciones(): Promise<void> {
  const capacitacion = capacitacionSeleccionada.value

  if (!capacitacion) {
    asignaciones.value = []
    return
  }

  cargandoAsignaciones.value = true

  try {
    asignaciones.value = await asignacionParticipanteService.listar(capacitacion.id)
  } catch (error) {
    console.error('Error al cargar asignaciones:', error)

    asignaciones.value = []

    await Swal.fire({
      icon: 'error',
      title: 'Error al cargar asignaciones',
      text: 'No se pudieron consultar los participantes registrados.',
      confirmButtonColor: '#17665e',
    })
  } finally {
    cargandoAsignaciones.value = false
  }
}

async function buscarParticipante(): Promise<void> {
  const dni = dniIngresado.value.trim()

  if (!/^\d{8}$/.test(dni)) {
    await Swal.fire({
      icon: 'warning',
      title: 'DNI inválido',
      text: 'Ingresa un DNI de exactamente 8 dígitos.',
      confirmButtonColor: '#17665e',
    })

    return
  }

  if (!capacitacionSeleccionada.value) {
    await Swal.fire({
      icon: 'warning',
      title: 'Capacitación requerida',
      text: 'Selecciona una capacitación antes de buscar al participante.',
      confirmButtonColor: '#17665e',
    })

    return
  }

  limpiarBusqueda()
  buscandoParticipante.value = true

  try {
    const respuesta = await api.get<Personal>(`/api/v1/personal/dni/${dni}`)

    const persona = respuesta.data

    if (!persona) {
      mensajeBusqueda.value = 'No se encontró ningún registro con ese DNI.'
      return
    }

    if (normalizarTexto(persona.nivel) !== 'NIVEL_3') {
      await Swal.fire({
        icon: 'warning',
        title: 'Nivel incorrecto',
        text: 'La persona consultada no pertenece al NIVEL 3.',
        confirmButtonColor: '#17665e',
      })

      return
    }

    participante.value = persona

    if (!participanteTieneCargoValido.value) {
      mensajeBusqueda.value = `El cargo "${persona.cargo}" no corresponde a APLICADOR, ORIENTADOR ni RESERVA.`
      return
    }

    const tipo = esReserva.value ? tipoAulaReserva.value : tipoAulaRequerido.value

    if (!tipo) {
      mensajeBusqueda.value = 'No se pudo determinar el tipo de aula requerido.'
      return
    }

    const aula = aulas.value.find((item) => {
      return item.activa && normalizarTexto(item.tipoAula) === tipo
    })

    if (!aula) {
      mensajeBusqueda.value = `No hay aulas activas de tipo ${tipo} para esta capacitación.`
      return
    }

    aulaSeleccionada.value = aula

    mensajeBusqueda.value = esReserva.value
      ? 'Participante de reserva identificado. Confirma el código de reserva y el aula de destino.'
      : `Participante identificado. Aula propuesta: ${aula.codigoAula}. El servidor determinará el aula definitiva al registrar la asignación.`
  } catch (error) {
    console.error('Error al buscar participante:', error)

    mensajeBusqueda.value =
      'No se pudo consultar el DNI. Verifica la conexión e inténtalo nuevamente.'
  } finally {
    buscandoParticipante.value = false
  }
}

function seleccionarAula(aula: AulaCapacitacion): void {
  aulaSeleccionada.value = aula
}

async function registrarAsignacion(): Promise<void> {
  const persona = participante.value
  const capacitacion = capacitacionSeleccionada.value
  const aula = aulaSeleccionada.value

  if (!persona || !capacitacion || !aula) {
    await Swal.fire({
      icon: 'warning',
      title: 'Datos incompletos',
      text: 'Selecciona un participante, una capacitación y un aula.',
      confirmButtonColor: '#17665e',
    })

    return
  }

  if (!participanteTieneCargoValido.value) {
    await Swal.fire({
      icon: 'warning',
      title: 'Cargo no válido',
      text: 'No se puede asignar una persona con este cargo.',
      confirmButtonColor: '#17665e',
    })

    return
  }

  if (esReserva.value && !codigoReserva.value.trim()) {
    await Swal.fire({
      icon: 'warning',
      title: 'Código requerido',
      text: 'Ingresa el código de reserva.',
      confirmButtonColor: '#17665e',
    })

    return
  }

  const confirmacion = await Swal.fire({
    icon: 'question',
    title: '¿Registrar asignación?',
    html: `
      <div style="text-align:left">
        <p><strong>DNI:</strong> ${persona.dni}</p>
        <p><strong>Participante:</strong> ${nombreCompleto(persona)}</p>
        <p><strong>Tipo de aula propuesto:</strong> ${aula.tipoAula}</p>
        <p><strong>Aula propuesta:</strong> ${aula.codigoAula}</p>
      </div>
      <p style="margin-top:12px;font-size:13px">
        El aula definitiva será la que confirme el servidor.
      </p>
    `,
    showCancelButton: true,
    confirmButtonText: 'Registrar asignación',
    cancelButtonText: 'Cancelar',
    confirmButtonColor: '#17665e',
  })

  if (!confirmacion.isConfirmed) {
    return
  }

  registrandoAsignacion.value = true

  try {
    const resultado = await asignacionParticipanteService.asignar(capacitacion.id, {
      dni: persona.dni,
      codigoReserva: esReserva.value ? codigoReserva.value.trim() : null,
    })

    // El resultado se obtiene del backend, no del aula propuesta.
    await Swal.fire({
      icon: 'success',
      title: 'Asignación registrada',
      html: `
        <div style="text-align:left">
          <p><strong>DNI:</strong> ${resultado.dni}</p>
          <p><strong>Participante:</strong>
            ${resultado.apellidoPaterno} ${resultado.apellidoMaterno}, ${resultado.nombres}
          </p>
          <p><strong>Aula asignada:</strong> ${resultado.codigoAula}</p>
          <p><strong>Tipo de aula:</strong> ${resultado.tipoAula}</p>
        </div>
      `,
      confirmButtonColor: '#17665e',
    })

    limpiarTodo()

    await cargarAsignaciones()
  } catch (error: unknown) {
    console.error('Error al registrar asignación:', error)

    let mensaje = 'No se pudo registrar la asignación. Inténtalo nuevamente.'

    if (typeof error === 'object' && error !== null && 'response' in error) {
      const respuesta = (
        error as {
          response?: {
            data?: {
              message?: string
              detail?: string
              title?: string
            }
            status?: number
          }
        }
      ).response

      mensaje =
        respuesta?.data?.message ?? respuesta?.data?.detail ?? respuesta?.data?.title ?? mensaje

      if (respuesta?.status === 409) {
        mensaje = 'El participante ya está asignado o existe un conflicto con la solicitud.'
      }
    }

    await Swal.fire({
      icon: 'error',
      title: 'No se pudo registrar la asignación',
      text: mensaje,
      confirmButtonColor: '#17665e',
    })
  } finally {
    registrandoAsignacion.value = false
  }
}

watch(capacitacionSeleccionada, async (nueva, anterior) => {
  if (!nueva || nueva.id === anterior?.id) {
    return
  }

  limpiarBusqueda()

  await Promise.all([cargarAulas(), cargarAsignaciones()])
})

watch(tipoAulaReserva, () => {
  if (!esReserva.value) {
    return
  }

  const aula = aulas.value.find((item) => {
    return item.activa && normalizarTexto(item.tipoAula) === tipoAulaReserva.value
  })

  aulaSeleccionada.value = aula ?? null
})

onMounted(async () => {
  await cargarCapacitaciones()
})
</script>

<template>
  <main class="min-h-screen bg-[#edf5f3] p-4 md:p-6 xl:p-8">
    <div class="mx-auto w-full max-w-[1600px] space-y-6">
      <!-- Encabezado -->
      <header class="rounded-2xl bg-[#17665e] p-6 text-white shadow-sm sm:p-8">
        <div class="flex items-center gap-4">
          <div class="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-white/15">
            <ClipboardCheck :size="30" />
          </div>

          <div>
            <h1 class="text-2xl font-bold sm:text-3xl">Asignación de participantes</h1>

            <p class="mt-2 text-sm text-white/85 sm:text-base">
              Busca participantes de NIVEL 3 por DNI y registra su asignación.
            </p>
          </div>
        </div>
      </header>

      <!-- Capacitación -->
      <section class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm sm:p-6">
        <div class="mb-4 flex items-center gap-3">
          <div
            class="flex h-10 w-10 items-center justify-center rounded-xl bg-[#e0f2ee] text-[#17665e]"
          >
            <Users :size="21" />
          </div>

          <div>
            <h2 class="font-semibold text-[#123f3b]">Capacitación</h2>

            <p class="text-sm text-slate-500">Selecciona la capacitación correspondiente.</p>
          </div>
        </div>

        <div class="flex flex-col gap-3 sm:flex-row sm:items-end">
          <div class="flex-1">
            <label for="capacitacion" class="mb-2 block text-sm font-medium text-[#123f3b]">
              Capacitación seleccionada
            </label>

            <select
              id="capacitacion"
              v-model="capacitacionSeleccionada"
              class="w-full rounded-xl border border-[#dbe9e5] bg-white px-4 py-3 text-sm outline-none focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/15"
              :disabled="cargandoCapacitaciones || capacitaciones.length === 0"
            >
              <option :value="null" disabled>Selecciona una capacitación</option>

              <option
                v-for="capacitacion in capacitaciones"
                :key="capacitacion.id"
                :value="capacitacion"
              >
                {{ capacitacion.nombre }} — {{ capacitacion.anio }} ({{ capacitacion.estado }})
              </option>
            </select>
          </div>
        </div>

        <div
          v-if="cargandoCapacitaciones"
          class="mt-4 flex items-center gap-2 text-sm text-slate-500"
        >
          <LoaderCircle :size="17" class="animate-spin" />
          Cargando capacitaciones...
        </div>

        <div v-if="errorCapacitaciones" class="mt-4 rounded-xl bg-red-50 p-3 text-sm text-red-700">
          {{ errorCapacitaciones }}
        </div>

        <div v-if="errorAulas" class="mt-4 rounded-xl bg-amber-50 p-3 text-sm text-amber-800">
          {{ errorAulas }}
        </div>
      </section>

      <!-- Búsqueda por DNI -->
      <section class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm sm:p-6">
        <div class="mb-5 flex items-center gap-3">
          <div
            class="flex h-10 w-10 items-center justify-center rounded-xl bg-[#e0f2ee] text-[#17665e]"
          >
            <Search :size="21" />
          </div>

          <div>
            <h2 class="font-semibold text-[#123f3b]">Buscar participante</h2>

            <p class="text-sm text-slate-500">Introduce el DNI de ocho dígitos.</p>
          </div>
        </div>

        <form class="flex flex-col gap-3 sm:flex-row" @submit.prevent="buscarParticipante">
          <div class="relative flex-1">
            <Search :size="19" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />

            <input
              v-model="dniIngresado"
              type="text"
              inputmode="numeric"
              autocomplete="off"
              maxlength="8"
              placeholder="Ingresa los 8 dígitos del DNI"
              class="w-full rounded-xl border border-[#dbe9e5] py-3 pl-10 pr-4 text-sm outline-none placeholder:text-slate-400 focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/15"
              :disabled="buscandoParticipante || !capacitacionSeleccionada"
              @input="dniIngresado = dniIngresado.replace(/\D/g, '').slice(0, 8)"
            />
          </div>

          <button
            type="submit"
            class="inline-flex items-center justify-center gap-2 rounded-xl bg-[#17665e] px-6 py-3 text-sm font-semibold text-white hover:bg-[#12544d] disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="
              buscandoParticipante || !capacitacionSeleccionada || dniIngresado.length !== 8
            "
          >
            <LoaderCircle v-if="buscandoParticipante" :size="18" class="animate-spin" />
            <Search v-else :size="18" />
            {{ buscandoParticipante ? 'Buscando...' : 'Buscar participante' }}
          </button>
        </form>

        <div
          v-if="mensajeBusqueda"
          class="mt-4 flex items-start gap-2 rounded-xl bg-[#f2f8f6] p-3 text-sm text-[#17665e]"
        >
          <AlertCircle :size="18" class="mt-0.5 shrink-0" />
          <p>{{ mensajeBusqueda }}</p>
        </div>
      </section>

      <!-- Datos del participante -->
      <section
        v-if="participante"
        class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm sm:p-6"
      >
        <div class="mb-5 flex items-center justify-between gap-3">
          <div class="flex items-center gap-3">
            <div
              class="flex h-10 w-10 items-center justify-center rounded-xl bg-[#e0f2ee] text-[#17665e]"
            >
              <UserRound :size="21" />
            </div>

            <div>
              <h2 class="font-semibold text-[#123f3b]">Datos del participante</h2>

              <p class="text-sm text-slate-500">Información recuperada del servicio de personal.</p>
            </div>
          </div>

          <button
            type="button"
            class="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
            aria-label="Limpiar participante"
            @click="limpiarBusqueda"
          >
            <X :size="19" />
          </button>
        </div>

        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <div class="rounded-xl bg-[#f6faf9] p-4">
            <p class="text-xs font-medium uppercase tracking-wide text-slate-500">DNI</p>
            <p class="mt-1 font-semibold text-[#123f3b]">{{ participante.dni }}</p>
          </div>

          <div class="rounded-xl bg-[#f6faf9] p-4 sm:col-span-2">
            <p class="text-xs font-medium uppercase tracking-wide text-slate-500">
              Apellidos y nombres
            </p>
            <p class="mt-1 font-semibold text-[#123f3b]">{{ nombreCompleto(participante) }}</p>
          </div>

          <div class="rounded-xl bg-[#f6faf9] p-4">
            <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Nivel</p>
            <p class="mt-1 font-semibold text-[#123f3b]">{{ participante.nivel }}</p>
          </div>

          <div class="rounded-xl bg-[#f6faf9] p-4">
            <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Cargo</p>
            <p class="mt-1 font-semibold text-[#123f3b]">{{ participante.cargo }}</p>
          </div>

          <div class="rounded-xl bg-[#f6faf9] p-4">
            <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Situación</p>
            <p class="mt-1 font-semibold text-[#123f3b]">
              {{ participante.situacion || 'No especificada' }}
            </p>
          </div>
        </div>

        <div
          v-if="participanteTieneCargoValido"
          class="mt-4 flex items-center gap-2 rounded-xl bg-emerald-50 p-3 text-sm text-emerald-800"
        >
          <CheckCircle2 :size="18" />
          Participante de NIVEL 3 identificado.
        </div>

        <div v-else class="mt-4 rounded-xl bg-amber-50 p-3 text-sm text-amber-800">
          El cargo no corresponde a APLICADOR, ORIENTADOR ni RESERVA.
        </div>
      </section>

      <!-- Código de reserva -->
      <section
        v-if="participante && esReserva"
        class="rounded-2xl border border-amber-200 bg-white p-5 shadow-sm sm:p-6"
      >
        <div class="mb-5 flex items-center gap-3">
          <div
            class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-100 text-amber-800"
          >
            <KeyRound :size="21" />
          </div>

          <div>
            <h2 class="font-semibold text-[#123f3b]">Participante de reserva</h2>
            <p class="text-sm text-slate-500">
              Ingresa el código de reserva para enviarlo al backend.
            </p>
          </div>
        </div>

        <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div>
            <label for="codigoReserva" class="mb-2 block text-sm font-medium text-[#123f3b]">
              Código de reserva
            </label>

            <input
              id="codigoReserva"
              v-model="codigoReserva"
              type="password"
              autocomplete="off"
              placeholder="Ingresa el código"
              class="w-full rounded-xl border border-[#dbe9e5] px-4 py-3 text-sm outline-none focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/15"
            />
          </div>

          <div>
            <label for="tipoAulaReserva" class="mb-2 block text-sm font-medium text-[#123f3b]">
              Tipo de aula propuesta
            </label>

            <select
              id="tipoAulaReserva"
              v-model="tipoAulaReserva"
              class="w-full rounded-xl border border-[#dbe9e5] bg-white px-4 py-3 text-sm outline-none focus:border-[#17665e] focus:ring-2 focus:ring-[#17665e]/15"
            >
              <option value="APLICADOR">Aplicador</option>
              <option value="ORIENTADOR">Orientador</option>
            </select>
          </div>
        </div>

        <div class="mt-4 rounded-xl bg-amber-50 p-3 text-sm text-amber-800">
          El código se enviará al endpoint de asignación para que el servidor aplique su validación.
          La selección de aula aquí es solo una propuesta visual.
        </div>
      </section>

      <!-- Selección de aula propuesta -->
      <section
        v-if="participante && participanteTieneCargoValido"
        class="rounded-2xl border border-[#dbe9e5] bg-white p-5 shadow-sm sm:p-6"
      >
        <div class="mb-5 flex items-center gap-3">
          <div
            class="flex h-10 w-10 items-center justify-center rounded-xl bg-[#e0f2ee] text-[#17665e]"
          >
            <DoorOpen :size="21" />
          </div>

          <div>
            <h2 class="font-semibold text-[#123f3b]">Aula propuesta</h2>

            <p class="text-sm text-slate-500">
              El servidor determina el aula definitiva durante la asignación.
            </p>
          </div>
        </div>

        <div v-if="cargandoAulas" class="flex items-center gap-2 py-4 text-sm text-slate-500">
          <LoaderCircle :size="18" class="animate-spin" />
          Cargando aulas...
        </div>

        <div
          v-else-if="aulasTipo.length === 0"
          class="rounded-xl bg-amber-50 p-4 text-sm text-amber-800"
        >
          No hay aulas activas del tipo requerido para esta capacitación.
        </div>

        <div v-else class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
          <button
            v-for="aula in aulasTipo"
            :key="aula.id"
            type="button"
            class="rounded-xl border p-4 text-left transition"
            :class="
              aulaSeleccionada?.id === aula.id
                ? 'border-[#17665e] bg-[#e0f2ee] ring-2 ring-[#17665e]/15'
                : 'border-[#dbe9e5] hover:border-[#17665e]/50 hover:bg-[#f6faf9]'
            "
            @click="seleccionarAula(aula)"
          >
            <div class="flex items-start justify-between gap-3">
              <div>
                <p class="font-semibold text-[#123f3b]">{{ aula.codigoAula }}</p>
                <p class="mt-1 text-sm text-slate-500">{{ aula.tipoAula }}</p>
              </div>

              <CheckCircle2
                v-if="aulaSeleccionada?.id === aula.id"
                :size="20"
                class="text-[#17665e]"
              />
            </div>

            <div class="mt-4 flex items-center gap-2 text-sm text-slate-600">
              <Users :size="17" />
              Aforo configurado: {{ aula.capacidad }}
            </div>
          </button>
        </div>

        <div
          v-if="aulaSeleccionada"
          class="mt-4 rounded-xl border border-[#dbe9e5] bg-[#f6faf9] p-4"
        >
          <p class="text-sm text-slate-500">Aula propuesta</p>
          <p class="mt-1 font-semibold text-[#123f3b]">{{ aulaSeleccionada.codigoAula }}</p>
          <p class="mt-1 text-sm text-slate-600">Tipo: {{ aulaSeleccionada.tipoAula }}</p>
          <p class="mt-2 text-xs leading-5 text-amber-700">
            La propuesta no reserva una plaza ni modifica la asignación que determine el backend.
          </p>
        </div>
      </section>

      <!-- Acciones -->
      <section v-if="participante" class="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
        <button
          type="button"
          class="inline-flex items-center justify-center gap-2 rounded-xl border border-[#dbe9e5] bg-white px-5 py-3 text-sm font-semibold text-[#17665e] hover:bg-[#f2f8f6]"
          @click="limpiarTodo"
        >
          Limpiar
        </button>

        <button
          type="button"
          class="inline-flex items-center justify-center gap-2 rounded-xl bg-[#17665e] px-5 py-3 text-sm font-semibold text-white hover:bg-[#12544d] disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="!puedeContinuar || registrandoAsignacion"
          @click="registrarAsignacion"
        >
          <LoaderCircle v-if="registrandoAsignacion" :size="18" class="animate-spin" />
          <ClipboardCheck v-else :size="18" />
          {{ registrandoAsignacion ? 'Registrando...' : 'Registrar asignación' }}
        </button>
      </section>

      <!-- Lista de asignaciones -->
      <section class="overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white shadow-sm">
        <div
          class="flex flex-col gap-3 border-b border-[#dbe9e5] p-5 sm:flex-row sm:items-center sm:justify-between"
        >
          <div>
            <h2 class="font-semibold text-[#123f3b]">Participantes asignados</h2>
            <p class="mt-1 text-sm text-slate-500">Asignaciones registradas por el servidor.</p>
          </div>
        </div>

        <div
          v-if="cargandoAsignaciones"
          class="flex items-center justify-center gap-2 p-8 text-sm text-slate-500"
        >
          <LoaderCircle :size="18" class="animate-spin" />
          Cargando asignaciones...
        </div>

        <div v-else-if="asignaciones.length === 0" class="p-8 text-center text-sm text-slate-500">
          No hay asignaciones registradas para esta capacitación.
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full min-w-190 text-left text-sm">
            <thead class="bg-[#f6faf9] text-xs uppercase text-slate-500">
              <tr>
                <th class="px-5 py-3 font-semibold">DNI</th>
                <th class="px-5 py-3 font-semibold">Participante</th>
                <th class="px-5 py-3 font-semibold">Cargo</th>
                <th class="px-5 py-3 font-semibold">Aula</th>
                <th class="px-5 py-3 font-semibold">Tipo</th>
                <th class="px-5 py-3 font-semibold">Fecha</th>
              </tr>
            </thead>

            <tbody class="divide-y divide-[#dbe9e5]">
              <tr
                v-for="asignacion in asignaciones"
                :key="asignacion.id"
                class="hover:bg-[#f6faf9]"
              >
                <td class="whitespace-nowrap px-5 py-4 font-medium text-[#123f3b]">
                  {{ asignacion.dni }}
                </td>

                <td class="px-5 py-4 text-[#123f3b]">
                  {{
                    [asignacion.apellidoPaterno, asignacion.apellidoMaterno, asignacion.nombres]
                      .filter(Boolean)
                      .join(' ')
                  }}
                </td>

                <td class="whitespace-nowrap px-5 py-4 text-slate-600">
                  {{ asignacion.cargo }}
                </td>

                <td class="whitespace-nowrap px-5 py-4 font-medium text-[#17665e]">
                  {{ asignacion.codigoAula }}
                </td>

                <td class="whitespace-nowrap px-5 py-4 text-slate-600">
                  {{ asignacion.tipoAula }}
                </td>

                <td class="whitespace-nowrap px-5 py-4 text-slate-600">
                  {{ new Date(asignacion.fechaAsignacion).toLocaleString('es-PE') }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <footer
        class="rounded-xl border border-[#dbe9e5] bg-white/70 p-4 text-xs leading-5 text-slate-500"
      >
        La asignación definitiva, la validación del código de reserva, la prevención de duplicados y
        el control del aforo dependen de la respuesta y las reglas del backend.
      </footer>
    </div>
  </main>
</template>
