<script setup lang="ts">
import { RouterLink } from 'vue-router'
import {
  ArrowLeft,
  ArrowRight,
  Compass,
  GraduationCap,
  House,
  SearchX,
} from 'lucide-vue-next'
</script>

<template>
  <div
    class="relative flex min-h-screen flex-col overflow-hidden bg-[#edf5f3] text-slate-800"
  >
    <!-- Decoración de fondo -->
    <div
      class="pointer-events-none absolute -right-32 -top-32 h-96 w-96 rounded-full bg-[#b9e3d9]/40 blur-3xl"
    ></div>

    <div
      class="pointer-events-none absolute -bottom-32 -left-32 h-96 w-96 rounded-full bg-[#cdebe4]/60 blur-3xl"
    ></div>

    <!-- Cabecera -->
    <header class="relative z-10 border-b border-[#d5e7e2] bg-white/90">
      <div class="mx-auto flex max-w-7xl items-center gap-3 px-5 py-4 sm:px-8">
        <div
          class="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#17665e] text-white"
        >
          <GraduationCap :size="25" />
        </div>

        <div>
          <h1 class="text-lg font-bold text-[#123f3b]">
            EDA 2026
          </h1>
          <p class="text-xs text-slate-500">
            Evaluación de Ascenso de Docentes
          </p>
        </div>
      </div>
    </header>

    <!-- Contenido -->
    <main
      class="relative z-10 flex flex-1 items-center justify-center px-5 py-12"
    >
      <section class="w-full max-w-2xl text-center">
        <!-- Icono principal -->
        <div class="relative mx-auto mb-8 flex h-32 w-32 items-center justify-center">
          <div class="absolute inset-0 rounded-full bg-[#d9eee8]"></div>

          <div
            class="relative flex h-24 w-24 items-center justify-center rounded-3xl bg-white text-[#17665e] shadow-lg shadow-[#123f3b]/10"
          >
            <SearchX :size="48" :stroke-width="1.5" />
          </div>

          <div
            class="absolute -right-1 -top-1 flex h-10 w-10 items-center justify-center rounded-full bg-[#17665e] text-white shadow-md"
          >
            <Compass :size="21" />
          </div>
        </div>

        <!-- Código de error -->
        <p
          class="text-7xl font-extrabold tracking-tight text-[#17665e] sm:text-8xl"
        >
          404
        </p>

        <h2 class="mt-5 text-2xl font-bold text-[#123f3b] sm:text-3xl">
          ¡Ups! Página no encontrada
        </h2>

        <p class="mx-auto mt-4 max-w-lg text-sm leading-7 text-slate-500 sm:text-base">
          La página que buscas no existe, ha cambiado de dirección
          o no está disponible. Verifica la dirección e intenta nuevamente.
        </p>

        <!-- Acciones -->
        <div class="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
          <RouterLink
            to="/"
            class="inline-flex items-center justify-center gap-2 rounded-xl bg-[#17665e] px-6 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#123f3b] focus:outline-none focus:ring-4 focus:ring-[#17665e]/20"
          >
            <House :size="18" />
            Ir al dashboard
            <ArrowRight :size="17" />
          </RouterLink>

          <button
            type="button"
            class="inline-flex items-center justify-center gap-2 rounded-xl border border-[#cfe2dc] bg-white px-6 py-3 text-sm font-semibold text-[#17665e] transition hover:bg-[#e0f2ee] focus:outline-none focus:ring-4 focus:ring-[#17665e]/10"
            @click="$router.back()"
          >
            <ArrowLeft :size="18" />
            Volver atrás
          </button>
        </div>

        <!-- Mensaje complementario -->
        <div
          class="mx-auto mt-10 flex max-w-md items-center gap-3 rounded-2xl border border-[#dbe9e5] bg-white/80 p-4 text-left"
        >
          <div
            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[#e0f2ee] text-[#17665e]"
          >
            <GraduationCap :size="21" />
          </div>

          <div>
            <p class="text-sm font-semibold text-[#123f3b]">
              Sistema EDA 2026
            </p>
            <p class="mt-1 text-xs leading-5 text-slate-500">
              Regresa al panel principal para continuar trabajando.
            </p>
          </div>
        </div>
      </section>
    </main>

    <!-- Pie de página -->
    <footer class="relative z-10 border-t border-[#d5e7e2] bg-white/70 px-5 py-5 text-center">
      <p class="text-xs text-slate-500">
        EDA 2026 · Sistema de Gestión de Evaluación de Ascenso de Docentes
      </p>
    </footer>
  </div>
</template>
