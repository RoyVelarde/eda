import { api } from '@/services/api'
import type { Personal } from '@/types/personal'

export interface ImportacionResponse {
  archivo: string
  nivel: 'NIVEL_2' | 'NIVEL_3'
  valido: boolean
  totalFilas: number
  filasValidas: number
  filasConError: number
  registrosNuevos: number
  registrosExistentes: number
  errores: string[]
}

export const personalService = {
  async listar(): Promise<Personal[]> {
    const respuesta = await api.get<Personal[]>('/api/v1/personal')
    return respuesta.data
  },
  async importarExcel(archivo: File, nivel: 'NIVEL_2' | 'NIVEL_3'): Promise<ImportacionResponse> {
    const formulario = new FormData()
    formulario.append('archivo', archivo)
    formulario.append('nivel', nivel)
    const respuesta = await api.post<ImportacionResponse>(
      '/api/v1/personal/importaciones/importar',
      formulario,
    )
    return respuesta.data
  },
  async obtenerPorId(id: string): Promise<Personal> {
    const respuesta = await api.get<Personal>(`/api/v1/personal/${id}`)

    return respuesta.data
  },
}
