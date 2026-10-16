<script setup lang="ts">
import { ref, watch } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { Menu, X } from 'lucide-vue-next'
import AppSidebar from '@/components/layout/AppSidebar.vue'

const route = useRoute()
const menuAbierto = ref(false)

// Cerrar el menú móvil al cambiar de página
watch(
  () => route.fullPath,
  () => {
    menuAbierto.value = false
  },
)
</script>

<template>
  <div class="min-h-screen bg-[#edf5f3]">
    <!-- Sidebar para computadoras -->
    <div class="fixed inset-y-0 left-0 z-30 hidden w-64 lg:block">
      <AppSidebar />
    </div>

    <!-- Sidebar para celulares -->
    <div
      v-if="menuAbierto"
      class="fixed inset-0 z-40 lg:hidden"
    >
      <!-- Fondo oscuro -->
      <button
        type="button"
        class="absolute inset-0 bg-black/50"
        aria-label="Cerrar menú"
        @click="menuAbierto = false"
      ></button>

      <!-- Menú -->
      <div class="absolute inset-y-0 left-0 w-64 shadow-xl">
        <AppSidebar />
      </div>

      <!-- Botón cerrar -->
      <button
        type="button"
        class="absolute left-67 top-4 rounded-xl bg-white p-2 text-[#123f3b] shadow-lg"
        aria-label="Cerrar menú"
        @click="menuAbierto = false"
      >
        <X :size="22" />
      </button>
    </div>

    <!-- Contenido principal -->
    <div class="min-h-screen lg:pl-64">
      <!-- Barra superior móvil -->
      <header
        class="flex items-center justify-between border-b border-[#d5e7e2] bg-white px-5 py-4 lg:hidden"
      >
        <div>
          <h1 class="text-lg font-bold text-[#123f3b]">
            EDA 2026
          </h1>
          <p class="text-xs text-slate-500">
            Sistema de gestión
          </p>
        </div>

        <button
          type="button"
          class="rounded-xl bg-[#e0f2ee] p-2 text-[#17665e]"
          aria-label="Abrir menú"
          @click="menuAbierto = true"
        >
          <Menu :size="23" />
        </button>
      </header>

      <!-- Página actual -->
      <main class="min-h-screen">
        <RouterView />
      </main>
    </div>
  </div>
</template>
