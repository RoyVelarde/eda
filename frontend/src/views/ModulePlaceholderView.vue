<script setup lang="ts">
import { Construction, ArrowLeft } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'

defineProps<{
  titulo: string
}>()
</script>

<template>
  <div class="mx-auto max-w-7xl px-5 py-8 sm:px-8">
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-[#123f3b]">
        {{ titulo }}
      </h1>

      <p class="mt-2 text-sm text-slate-500">
        EDA 2026 · Gestión de {{ titulo.toLowerCase() }}
      </p>
    </div>

    <div
      class="flex min-h-80 flex-col items-center justify-center rounded-3xl border border-[#dbe9e5] bg-white p-8 text-center shadow-sm"
    >
      <div
        class="mb-5 flex h-16 w-16 items-center justify-center rounded-2xl bg-[#e0f2ee] text-[#17665e]"
      >
        <Construction :size="32" />
      </div>

      <h2 class="text-xl font-bold text-[#123f3b]">
        Módulo en desarrollo
      </h2>

      <p class="mt-3 max-w-md text-sm leading-6 text-slate-500">
        Estamos preparando las funcionalidades del módulo
        {{ titulo }}.
      </p>

      <RouterLink
        to="/"
        class="mt-6 inline-flex items-center gap-2 rounded-xl bg-[#17665e] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#123f3b]"
      >
        <ArrowLeft :size="17" />
        Volver al dashboard
      </RouterLink>
    </div>
  </div>
</template>
