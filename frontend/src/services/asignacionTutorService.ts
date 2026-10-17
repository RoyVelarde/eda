import { api } from '@/services/api'

import type {
  AsignacionTutor,
  AsignacionTutorRequest,
  ActualizarAsignacionTutorRequest,
  AulaCapacitacion,
} from '@/types/asignacionTutor'

const BASE_URL = '/api/v1/capacitaciones'

async function listarAulas(capacitacionId: string): Promise<AulaCapacitacion[]> {
  const respuesta = await api.get<AulaCapacitacion[]>(`${BASE_URL}/${capacitacionId}/aulas`)

  return respuesta.data
}

async function listarTutores(capacitacionId: string): Promise<AsignacionTutor[]> {
  const respuesta = await api.get<AsignacionTutor[]>(`${BASE_URL}/${capacitacionId}/tutores`)

  return respuesta.data
}

async function asignarTutor(
  capacitacionId: string,
  aulaCapacitacionId: string,
  datos: AsignacionTutorRequest,
): Promise<AsignacionTutor> {
  const respuesta = await api.post<AsignacionTutor>(
    `${BASE_URL}/${capacitacionId}/aulas/${aulaCapacitacionId}/tutores`,
    datos,
  )

  return respuesta.data
}

async function actualizarAsignacion(
  capacitacionId: string,
  asignacionTutorId: string,
  datos: ActualizarAsignacionTutorRequest,
): Promise<AsignacionTutor> {
  const respuesta = await api.put<AsignacionTutor>(
    `${BASE_URL}/${capacitacionId}/tutores/${asignacionTutorId}`,
    datos,
  )

  return respuesta.data
}

async function quitarTutor(capacitacionId: string, asignacionTutorId: string): Promise<void> {
  await api.delete(`${BASE_URL}/${capacitacionId}/tutores/${asignacionTutorId}`)
}

export const asignacionTutorService = {
  listarAulas,
  listarTutores,
  asignarTutor,
  actualizarAsignacion,
  quitarTutor,
}
