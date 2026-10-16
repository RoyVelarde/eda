<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Activity,
  ArrowRight,
  Eye,
  EyeOff,
  GraduationCap,
  LockKeyhole,
  ShieldCheck,
  UserRound,
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const username = ref('')
const password = ref('')
const mostrarPassword = ref(false)
const cargando = ref(false)
const error = ref('')

async function iniciarSesion() {
  error.value = ''
  if (!username.value.trim() || !password.value) {
    error.value = 'Ingresa tu usuario y contraseña.'
    return
  }
  cargando.value = true
  try {
    await authStore.iniciarSesion({
      username: username.value.trim(),
      password: password.value,
    })
    await router.push({ name: 'dashboard' })
  } catch (err: unknown) {
    console.error('Error de inicio de sesión:', err)
    if (typeof err === 'object' && err !== null && 'response' in err) {
      const errorHttp = err as {
        response?: {
          status?: number
        }
      }
      if (errorHttp.response?.status === 401) {
        error.value = 'Usuario o contraseña incorrectos.'
      } else if (errorHttp.response?.status === 403) {
        error.value = 'No tienes permiso para iniciar sesión.'
      } else {
        error.value = 'No se pudo iniciar sesión. Inténtalo nuevamente.'
      }
    } else {
      error.value ='No se pudo conectar con el servidor. Verifica que esté funcionando.'
    }
  } finally {
    cargando.value = false
  }
}
</script>

<template>
  <main
    class="relative flex min-h-screen items-center justify-center overflow-hidden bg-[#edf5f3] px-4 py-10"
  >
    <!-- Decoración del fondo -->
    <div
      class="pointer-events-none absolute -left-32 -top-32 h-96 w-96 rounded-full bg-teal-200/40 blur-3xl"
    ></div>
    <div
      class="pointer-events-none absolute -bottom-40 -right-24 h-96 w-96 rounded-full bg-emerald-200/50 blur-3xl"
    ></div>
    <section
      class="relative grid w-full max-w-5xl overflow-hidden rounded-3xl bg-white shadow-2xl shadow-teal-950/10 ring-1 ring-slate-200/70 md:grid-cols-2"
    >
      <!-- Panel visual -->
      <aside
        class="relative hidden flex-col justify-between overflow-hidden bg-[#123f3b] p-10 text-white md:flex lg:p-12"
      >
        <div
          class="pointer-events-none absolute -right-24 top-24 h-72 w-72 rounded-full border border-teal-300/20"
        ></div>
        <div
          class="pointer-events-none absolute -right-12 top-36 h-48 w-48 rounded-full border border-teal-300/20"
        ></div>
        <div class="relative">
          <div class="mt-20">
            <p
              class="inline-flex items-center gap-2 rounded-full border border-teal-300/20 bg-teal-300/10 px-3 py-1.5 text-xs font-semibold tracking-wide text-teal-200"
            >
              <Activity :size="15" />
              SISTEMA DE GESTIÓN
            </p>
            <h1 class="mt-6 text-4xl font-extrabold leading-tight lg:text-5xl">
              Evaluación de
              <span class="text-teal-300">Ascenso</span>
              de Docentes 2026
            </h1>
            <p class="mt-6 max-w-sm text-base leading-7 text-slate-300">
              Accede a la plataforma para gestionar las actividades de la
              evaluación docente de manera organizada y segura.
            </p>
          </div>
        </div>
        <div
          class="relative mt-16 flex items-center gap-3 border-t border-white/10 pt-6"
        >
          <ShieldCheck class="shrink-0 text-teal-300" :size="22" />

          <p class="text-sm leading-6 text-slate-300">
            Acceso seguro para personal autorizado.
          </p>
        </div>
      </aside>
      <!-- Formulario -->
      <section class="px-6 py-10 sm:px-10 md:px-10 lg:px-14 lg:py-14">
        <div class="mx-auto max-w-md">
          <div
            class="flex h-14 w-14 items-center justify-center rounded-2xl bg-teal-50 text-teal-800 md:hidden"
          >
            <GraduationCap :size="31" />
          </div>
          <div class="mt-5 md:mt-4">
            <p
              class="text-sm font-bold uppercase tracking-[0.18em] text-teal-700"
            >
              Bienvenido
            </p>
            <h2 class="mt-2 text-3xl font-extrabold tracking-tight text-slate-800">
              Iniciar sesión
            </h2>
            <p class="mt-3 text-sm leading-6 text-slate-500">
              Ingresa tus credenciales para acceder al sistema.
            </p>
          </div>

          <form class="mt-8 space-y-5" @submit.prevent="iniciarSesion">
            <!-- Usuario -->
            <div>
              <label
                for="username"
                class="mb-2 block text-sm font-semibold text-slate-700"
              >
                Nombre de usuario
              </label>
              <div class="relative">
                <UserRound
                  class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
                  :size="19"
                />
                <input
                  id="username"
                  v-model.trim="username"
                  type="text"
                  name="username"
                  autocomplete="username"
                  placeholder="Ingresa tu usuario"
                  required
                  :disabled="cargando"
                  class="w-full rounded-xl border border-slate-200 bg-slate-50 py-3.5 pl-12 pr-4 text-sm text-slate-800 outline-none transition placeholder:text-slate-400 focus:border-teal-600 focus:bg-white focus:ring-4 focus:ring-teal-100 disabled:cursor-not-allowed disabled:opacity-60"
                />
              </div>
            </div>

            <!-- Contraseña -->
            <div>
              <label
                for="password"
                class="mb-2 block text-sm font-semibold text-slate-700"
              >
                Contraseña
              </label>
              <div class="relative">
                <LockKeyhole
                  class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
                  :size="19"
                />
                <input
                  id="password"
                  v-model="password"
                  :type="mostrarPassword ? 'text' : 'password'"
                  name="password"
                  autocomplete="current-password"
                  placeholder="Ingresa tu contraseña"
                  required
                  :disabled="cargando"
                  class="w-full rounded-xl border border-slate-200 bg-slate-50 py-3.5 pl-12 pr-12 text-sm text-slate-800 outline-none transition placeholder:text-slate-400 focus:border-teal-600 focus:bg-white focus:ring-4 focus:ring-teal-100 disabled:cursor-not-allowed disabled:opacity-60"
                />
                <button
                  type="button"
                  :disabled="cargando"
                  :aria-label="
                    mostrarPassword
                      ? 'Ocultar contraseña'
                      : 'Mostrar contraseña'
                  "
                  class="absolute right-3 top-1/2 -translate-y-1/2 rounded-lg p-2 text-slate-400 transition hover:bg-teal-50 hover:text-teal-800 focus:outline-none focus:ring-2 focus:ring-teal-500 disabled:opacity-50"
                  @click="mostrarPassword = !mostrarPassword"
                >
                  <EyeOff v-if="mostrarPassword" :size="18" />
                  <Eye v-else :size="18" />
                </button>
              </div>
            </div>

            <!-- Error -->
            <div
              v-if="error"
              role="alert"
              aria-live="polite"
              class="flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 p-3.5 text-sm text-red-700"
            >
              <ShieldCheck class="mt-0.5 shrink-0" :size="18" />
              <p>{{ error }}</p>
            </div>
            <!-- Botón -->
            <button
              type="submit"
              :disabled="cargando"
              class="group flex w-full items-center justify-center gap-2 rounded-xl bg-[#17665e] px-5 py-3.5 text-sm font-bold text-white shadow-lg shadow-teal-900/15 transition hover:bg-[#104f49] focus:outline-none focus:ring-4 focus:ring-teal-200 disabled:cursor-not-allowed disabled:opacity-60"
            >
              <span>
                {{ cargando ? 'Verificando credenciales...' : 'Ingresar al sistema' }}
              </span>

              <ArrowRight
                v-if="!cargando"
                class="transition-transform group-hover:translate-x-1"
                :size="19"
              />
              <span
                v-else
                class="h-5 w-5 animate-spin rounded-full border-2 border-white/40 border-t-white"
                aria-hidden="true"
              ></span>
            </button>
          </form>
          <!-- Pie -->
          <div class="mt-8 border-t border-slate-100 pt-5">
            <div class="flex items-center justify-center gap-2 text-slate-400">
              <ShieldCheck :size="16" />
              <p class="text-xs">
                Acceso exclusivo para personal autorizado
              </p>
            </div>
            <p class="mt-4 text-center text-xs text-slate-400">
              eda · Evaluación de Ascenso de Docentes 2026
            </p>
          </div>
        </div>
      </section>
    </section>
  </main>
</template>
