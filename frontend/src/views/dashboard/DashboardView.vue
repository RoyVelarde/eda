<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function cerrarSesion() {
  authStore.cerrarSesion()
  router.replace({ name: 'login' })
}
</script>

<template>
  <main class="min-h-screen bg-slate-100 p-6 sm:p-10">
    <section class="mx-auto max-w-5xl">
      <header
        class="flex flex-col gap-4 rounded-2xl bg-blue-800 p-6 text-white shadow-lg sm:flex-row sm:items-center sm:justify-between"
      >
        <div>
          <p class="text-sm font-semibold uppercase tracking-widest text-blue-200">
            Sistema de gestión
          </p>
          <h1 class="mt-2 text-3xl font-extrabold">
            eda
          </h1>
          <p class="mt-2 text-sm text-blue-100">
            Evaluación de Ascenso de Docentes 2026
          </p>
        </div>
        <button
          type="button"
          class="rounded-xl bg-white px-5 py-3 font-bold text-blue-800 transition hover:bg-blue-50 focus:outline-none focus:ring-4 focus:ring-blue-300"
          @click="cerrarSesion"
        >
          Cerrar sesión
        </button>
      </header>

      <section class="mt-8 grid gap-6 md:grid-cols-2">
        <article class="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <h2 class="text-lg font-bold text-slate-800">
            Bienvenido
          </h2>
          <p class="mt-2 text-slate-600">
            {{ authStore.usuario?.nombres }}
            {{ authStore.usuario?.apellidos }}
          </p>
          <p class="mt-1 text-sm text-slate-500">
            Usuario: {{ authStore.usuario?.username }}
          </p>
        </article>
        <article class="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <h2 class="text-lg font-bold text-slate-800">
            Tu rol
          </h2>
          <div class="mt-4">
            <span
              class="inline-flex rounded-full bg-blue-100 px-4 py-2 text-sm font-bold text-blue-800"
            >
              {{ authStore.rol }}
            </span>
          </div>
          <p class="mt-3 text-sm text-slate-500">
            Los módulos disponibles se configurarán según tus permisos.
          </p>
        </article>
      </section>

      <section class="mt-8 rounded-2xl border border-dashed border-slate-300 bg-white p-8 text-center">
        <h2 class="text-xl font-bold text-slate-800">
          Panel principal
        </h2>
        <p class="mx-auto mt-3 max-w-xl text-slate-600">
          Estamos preparando los módulos de personal, aulas, capacitaciones,
          participantes y reportes.
        </p>
      </section>
    </section>
  </main>
</template>
