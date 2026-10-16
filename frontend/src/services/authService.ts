import { api } from '@/services/api'
import type { LoginRequest, LoginResponse } from '@/types/auth'

export const authService = {
  async login(datos: LoginRequest): Promise<LoginResponse> {
    const respuesta = await api.post<LoginResponse>('/api/v1/auth/login', datos)
    return respuesta.data
  },
}
