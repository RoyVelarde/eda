import { api } from '@/services/api'
import type { AulaCapacitacion, AulaCapacitacionRequest } from '@/types/aula'

const BASE_URL = '/api/v1/capacitaciones'

async function listar(capacitacionId: string): Promise<AulaCapacitacion[]> {
  const respuesta = await api.get<AulaCapacitacion[]>(`${BASE_URL}/${capacitacionId}/aulas`)
  return respuesta.data
}

async function asignar(
  capacitacionId: string,
  request: AulaCapacitacionRequest,
): Promise<AulaCapacitacion> {
  const respuesta = await api.post<AulaCapacitacion>(`${BASE_URL}/${capacitacionId}/aulas`, request)
  return respuesta.data
}

export const aulaCapacitacionService = {
  listar,
  asignar,
}
