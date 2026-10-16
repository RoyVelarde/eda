import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authService } from '@/services/authService'
import type { AuthSession, LoginRequest, Usuario } from '@/types/auth'

const STORAGE_KEY = 'eda_auth'

function cargarSesion(): AuthSession | null {
  try {
    const contenido = sessionStorage.getItem(STORAGE_KEY)
    if (!contenido) return null
    const sesion = JSON.parse(contenido) as AuthSession
    if (
      !sesion.accessToken ||
      !sesion.expiresAt ||
      sesion.expiresAt <= Date.now() ||
      !sesion.usuario?.id
    ) {
      sessionStorage.removeItem(STORAGE_KEY)
      return null
    }
    return sesion
  } catch {
    sessionStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const sesionInicial = cargarSesion()
  const accessToken = ref<string | null>(sesionInicial?.accessToken ?? null)
  const usuario = ref<Usuario | null>(sesionInicial?.usuario ?? null)
  const expiresAt = ref<number | null>(sesionInicial?.expiresAt ?? null)
  const autenticado = computed(() => {
    return Boolean(
      accessToken.value && usuario.value && expiresAt.value && expiresAt.value > Date.now(),
    )
  })

  const rol = computed(() => usuario.value?.rol ?? null)
  async function iniciarSesion(datos: LoginRequest): Promise<void> {
    const respuesta = await authService.login(datos)
    if (!respuesta.accessToken || !respuesta.usuario?.id) {
      throw new Error('El servidor devolvió una respuesta de autenticación inválida.')
    }
    if (!respuesta.usuario.activo) {
      throw new Error('El usuario está inactivo.')
    }
    const nuevaSesion: AuthSession = {
      accessToken: respuesta.accessToken,
      expiresAt: Date.now() + respuesta.expiresIn * 1000,
      usuario: respuesta.usuario,
    }
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(nuevaSesion))
    accessToken.value = nuevaSesion.accessToken
    usuario.value = nuevaSesion.usuario
    expiresAt.value = nuevaSesion.expiresAt
  }

  function cerrarSesion(): void {
    sessionStorage.removeItem(STORAGE_KEY)
    accessToken.value = null
    usuario.value = null
    expiresAt.value = null
  }

  return {
    accessToken,
    usuario,
    expiresAt,
    autenticado,
    rol,
    iniciarSesion,
    cerrarSesion,
  }
})
