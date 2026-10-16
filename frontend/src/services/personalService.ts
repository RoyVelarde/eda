import { api } from '@/services/api'
import type { Personal } from '@/types/personal'

export const personalService = {
  async listar(): Promise<Personal[]> {
    const respuesta = await api.get<Personal[]>('/api/v1/personal')
    return respuesta.data
  },
}
