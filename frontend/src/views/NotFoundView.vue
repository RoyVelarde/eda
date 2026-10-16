<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
function volver() {
  router.back()
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-slate-100 px-4 py-10">
    <section class="w-full max-w-lg rounded-2xl bg-white p-8 text-center shadow-xl ring-1 ring-slate-200 sm:p-12">
      <div class="text-7xl font-extrabold tracking-tight text-blue-800">
        404
      </div>
      <h1 class="mt-4 text-2xl font-bold text-slate-800">
        Página no encontrada
      </h1>
      <p class="mt-3 leading-7 text-slate-600">
        La página que buscas no existe o la dirección ingresada es incorrecta.
      </p>
      <div class="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
        <button
          type="button"
          class="rounded-xl bg-blue-700 px-6 py-3 font-semibold text-white transition hover:bg-blue-800 focus:outline-none focus:ring-4 focus:ring-blue-200"
          @click="router.push({ name: 'dashboard' })"
        >
          Ir al inicio
        </button>
        <button
          type="button"
          class="rounded-xl border border-slate-300 px-6 py-3 font-semibold text-slate-700 transition hover:bg-slate-50 focus:outline-none focus:ring-4 focus:ring-slate-200"
          @click="volver"
        >
          Volver atrás
        </button>
      </div>
      <p class="mt-8 text-sm font-bold tracking-wide text-blue-800">
        eda
      </p>
    </section>
  </main>
</template>
