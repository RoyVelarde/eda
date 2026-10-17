import { api } from '@/services/api'
import type { Capacitacion, CapacitacionRequest, EstadoCapacitacion } from '@/types/capacitacion'

const BASE_URL = '/api/v1/capacitaciones'

async function listar(): Promise<Capacitacion[]> {
  const respuesta = await api.get<Capacitacion[]>(BASE_URL)
  return respuesta.data
}

async function obtenerPorId(id: string): Promise<Capacitacion> {
  const respuesta = await api.get<Capacitacion>(`${BASE_URL}/${id}`)
  return respuesta.data
}

async function listarPorAnio(anio: number): Promise<Capacitacion[]> {
  const respuesta = await api.get<Capacitacion[]>(`${BASE_URL}/anio/${anio}`)
  return respuesta.data
}

async function listarPorEstado(estado: EstadoCapacitacion): Promise<Capacitacion[]> {
  const respuesta = await api.get<Capacitacion[]>(`${BASE_URL}/estado/${estado}`)
  return respuesta.data
}

async function registrar(datos: CapacitacionRequest): Promise<Capacitacion> {
  const respuesta = await api.post<Capacitacion>(BASE_URL, datos)
  return respuesta.data
}

async function iniciar(id: string): Promise<Capacitacion> {
  const respuesta = await api.patch<Capacitacion>(`${BASE_URL}/${id}/iniciar`)
  return respuesta.data
}

export const capacitacionService = {
  listar,
  obtenerPorId,
  listarPorAnio,
  listarPorEstado,
  registrar,
  iniciar,
}
