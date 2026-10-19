import { api } from '@/services/api'
import type {
  AsignacionParticipanteRequest,
  AsignacionParticipanteResponse,
  CambiarAsignacionParticipanteRequest,
  CodigoReservaRequest,
  CodigoReservaResponse,
} from '@/types/asignacionParticipante'

const BASE_URL = '/api/v1/capacitaciones'

export const asignacionParticipanteService = {
  async asignar(
    capacitacionId: string,
    request: AsignacionParticipanteRequest,
  ): Promise<AsignacionParticipanteResponse> {
    const respuesta = await api.post<AsignacionParticipanteResponse>(
      `${BASE_URL}/${capacitacionId}/participantes/asignaciones`,
      request,
    )
    return respuesta.data
  },

  async listar(capacitacionId: string): Promise<AsignacionParticipanteResponse[]> {
    const respuesta = await api.get<AsignacionParticipanteResponse[]>(
      `${BASE_URL}/${capacitacionId}/participantes/asignaciones`,
    )
    return respuesta.data
  },

  async cambiarAsignacion(
    capacitacionId: string,
    asignacionId: string,
    request: CambiarAsignacionParticipanteRequest,
  ): Promise<AsignacionParticipanteResponse> {
    const respuesta = await api.patch<AsignacionParticipanteResponse>(
      `${BASE_URL}/${capacitacionId}/participantes/${asignacionId}/asignacion`,
      request,
    )
    return respuesta.data
  },

  async consultarCodigoReserva(capacitacionId: string): Promise<CodigoReservaResponse> {
    const respuesta = await api.get<CodigoReservaResponse>(
      `${BASE_URL}/${capacitacionId}/codigo-reserva`,
    )
    return respuesta.data
  },

  async configurarCodigoReserva(
    capacitacionId: string,
    request: CodigoReservaRequest,
  ): Promise<CodigoReservaResponse> {
    const respuesta = await api.post<CodigoReservaResponse>(
      `${BASE_URL}/${capacitacionId}/codigo-reserva`,
      request,
    )
    return respuesta.data
  },

  async desactivarCodigoReserva(capacitacionId: string): Promise<void> {
    await api.delete(`${BASE_URL}/${capacitacionId}/codigo-reserva`)
  },
}
