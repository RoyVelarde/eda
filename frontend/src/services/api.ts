import axios from 'axios'
import type { AuthSession } from '@/types/auth'

const STORAGE_KEY = 'eda_auth'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  headers: {
    Accept: 'application/json',
  },
})

api.interceptors.request.use((config) => {
  try {
    const contenido = sessionStorage.getItem(STORAGE_KEY)
    if (!contenido) {
      return config
    }
    const sesion = JSON.parse(contenido) as AuthSession
    if (!sesion.accessToken || !sesion.expiresAt || sesion.expiresAt <= Date.now()) {
      sessionStorage.removeItem(STORAGE_KEY)
      return config
    }
    config.headers.set('Authorization', `Bearer ${sesion.accessToken}`)
  } catch {
    sessionStorage.removeItem(STORAGE_KEY)
  }

  return config
})
