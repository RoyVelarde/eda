import { api } from '@/services/api'
import type { Aula, AulaRequest, TipoAula } from '@/types/aula'

const BASE_URL = '/api/v1/aulas'

async function listar(): Promise<Aula[]> {
  const respuesta = await api.get<Aula[]>(BASE_URL)
  return respuesta.data
}

async function listarActivas(): Promise<Aula[]> {
  const respuesta = await api.get<Aula[]>(`${BASE_URL}/activas`)
  return respuesta.data
}

async function listarPorTipo(tipo: TipoAula): Promise<Aula[]> {
  const respuesta = await api.get<Aula[]>(`${BASE_URL}/tipo/${tipo}`)
  return respuesta.data
}

async function obtenerPorId(id: string): Promise<Aula> {
  const respuesta = await api.get<Aula>(`${BASE_URL}/${id}`)
  return respuesta.data
}

async function buscarPorCodigo(codigo: string): Promise<Aula> {
  const respuesta = await api.get<Aula>(`${BASE_URL}/codigo/${encodeURIComponent(codigo)}`)

  return respuesta.data
}

async function registrar(datos: AulaRequest): Promise<Aula> {
  const respuesta = await api.post<Aula>(BASE_URL, datos)
  return respuesta.data
}

export const aulaService = {
  listar,
  listarActivas,
  listarPorTipo,
  obtenerPorId,
  buscarPorCodigo,
  registrar,
}
