<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  AlertCircle,
  BriefcaseBusiness,
  ChevronDown,
  LoaderCircle,
  Mail,
  RefreshCw,
  Search,
  UsersRound,
  X,
} from 'lucide-vue-next'

import { personalService } from '@/services/personalService'
import type { NivelPersonal, Personal } from '@/types/personal'

const personal = ref<Personal[]>([])
const cargando = ref(false)
const error = ref('')
const busqueda = ref('')
const nivelSeleccionado = ref<'TODOS' | NivelPersonal>('TODOS')
const cargoSeleccionado = ref('TODOS')

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
      nivelSeleccionado.value === 'TODOS' ||
      persona.nivel === nivelSeleccionado.value

    const coincideCargo =
      cargoSeleccionado.value === 'TODOS' ||
      persona.cargo === cargoSeleccionado.value

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

function limpiarFiltros() {
  busqueda.value = ''
  nivelSeleccionado.value = 'TODOS'
  cargoSeleccionado.value = 'TODOS'
}

function nombreCompleto(persona: Personal) {
  return [
    persona.apellidoPaterno,
    persona.apellidoMaterno,
    persona.nombres,
  ]
    .filter(Boolean)
    .join(' ')
}

onMounted(cargarPersonal)
</script>

<template>
  <div class="min-h-screen bg-[#edf5f3] px-5 py-8 sm:px-8">
    <div class="mx-auto max-w-7xl">
      <!-- Encabezado -->
      <header
        class="mb-8 flex flex-col justify-between gap-4 sm:flex-row sm:items-center"
      >
        <div>
          <div class="mb-2 flex items-center gap-2 text-sm text-[#17665e]">
            <UsersRound :size="18" />
            Gestión del personal
          </div>

          <h1 class="text-2xl font-bold text-[#123f3b] sm:text-3xl">
            Personal
          </h1>

          <p class="mt-2 text-sm text-slate-500">
            Consulta y filtra los registros de los niveles 2 y 3.
          </p>
        </div>

        <button
          type="button"
          :disabled="cargando"
          class="inline-flex items-center justify-center gap-2 self-start rounded-xl bg-[#17665e] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#123f3b] disabled:cursor-not-allowed disabled:opacity-60 sm:self-auto"
          @click="cargarPersonal"
        >
          <LoaderCircle v-if="cargando" :size="18" class="animate-spin" />
          <RefreshCw v-else :size="18" />
          Actualizar
        </button>
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

      <!-- Tabla y filtros -->
      <section class="overflow-hidden rounded-2xl border border-[#dbe9e5] bg-white shadow-sm">
        <div class="border-b border-[#e7efec] p-5 sm:p-6">
          <div
            class="mb-5 flex flex-col justify-between gap-3 sm:flex-row sm:items-center"
          >
            <div>
              <h2 class="text-lg font-bold text-[#123f3b]">
                Directorio de personal
              </h2>
              <p class="mt-1 text-sm text-slate-500">
                {{ personalFiltrado.length }} de {{ personal.length }} registros
              </p>
            </div>

            <button
              v-if="
                busqueda ||
                nivelSeleccionado !== 'TODOS' ||
                cargoSeleccionado !== 'TODOS'
              "
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
              <Search
                :size="18"
                class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
              />
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
                <option
                  v-for="nivel in niveles"
                  :key="nivel.valor"
                  :value="nivel.valor"
                >
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
                <option
                  v-for="cargo in cargos"
                  :key="cargo.valor"
                  :value="cargo.valor"
                >
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
          <p class="mt-4 font-semibold text-[#123f3b]">
            Cargando personal...
          </p>
          <p class="mt-1 text-sm text-slate-500">
            Consultando el servidor.
          </p>
        </div>

        <!-- Error -->
        <div
          v-else-if="error"
          class="flex flex-col items-center px-6 py-16 text-center"
        >
          <div class="rounded-2xl bg-red-50 p-4 text-red-600">
            <AlertCircle :size="30" />
          </div>
          <h3 class="mt-4 font-bold text-[#123f3b]">
            No se pudo cargar la información
          </h3>
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
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Sin resultados -->
        <div v-else class="flex flex-col items-center px-6 py-16 text-center">
          <div class="rounded-2xl bg-[#e0f2ee] p-4 text-[#17665e]">
            <Search :size="30" />
          </div>

          <h3 class="mt-4 font-bold text-[#123f3b]">
            No se encontraron registros
          </h3>

          <p class="mt-2 text-sm text-slate-500">
            Prueba con otro término o cambia los filtros.
          </p>

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
            Mostrando {{ personalFiltrado.length }} registros.
            Los resultados corresponden al personal devuelto por el servidor.
          </p>
        </div>
      </section>
    </div>
  </div>
</template>
