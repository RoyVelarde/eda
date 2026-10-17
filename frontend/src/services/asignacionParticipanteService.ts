import { api } from '@/services/api'

const BASE_URL = '/api/v1/capacitaciones'

/**
 * Datos enviados al registrar una asignación.
 * Corresponde a AsignacionParticipanteRequest del backend.
 */
export interface AsignacionParticipanteRequest {
  dni: string
  codigoReserva?: string | null
}

/**
 * Respuesta del backend al registrar o consultar una asignación.
 * Corresponde a AsignacionParticipanteResponse.
 */
export interface AsignacionParticipanteResponse {
  id: string
  capacitacionId: string
  personalId: string
  dni: string
  apellidoPaterno: string
  apellidoMaterno: string
  nombres: string
  cargo: string
  funcion: string
  situacion: string | null
  ranking: number | null
  aulaCapacitacionId: string
  aulaId: string
  codigoAula: string
  tipoAula: string
  fechaAsignacion: string
}

/**
 * Datos para cambiar la asignación de un participante.
 * Corresponde a CambiarAsignacionParticipanteRequest.
 */
export interface CambiarAsignacionParticipanteRequest {
  aulaCapacitacionId: string
  funcion: string
}

/**
 * Respuesta de consulta del código de reserva.
 * Corresponde a CodigoReservaResponse.
 */
export interface CodigoReservaResponse {
  id: string
  capacitacionId: string
  activo: boolean
  fechaCreacion: string
  fechaActualizacion: string
}

/**
 * Servicio de asignación de participantes.
 */
export const asignacionParticipanteService = {
  /**
   * Registra la asignación de un participante.
   * POST /api/v1/capacitaciones/{capacitacionId}/participantes/asignaciones
   */
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

  /**
   * Lista las asignaciones de una capacitación.
   * GET /api/v1/capacitaciones/{capacitacionId}/participantes/asignaciones
   */
  async listar(capacitacionId: string): Promise<AsignacionParticipanteResponse[]> {
    const respuesta = await api.get<AsignacionParticipanteResponse[]>(
      `${BASE_URL}/${capacitacionId}/participantes/asignaciones`,
    )

    return respuesta.data
  },

  /**
   * Cambia el aula asignada a un participante.
   * PATCH /api/v1/capacitaciones/{capacitacionId}/participantes/{asignacionId}/asignacion
   */
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

  /**
   * Consulta el estado del código de reserva configurado.
   * GET /api/v1/capacitaciones/{capacitacionId}/codigo-reserva
   */
  async consultarCodigoReserva(capacitacionId: string): Promise<CodigoReservaResponse> {
    const respuesta = await api.get<CodigoReservaResponse>(
      `${BASE_URL}/${capacitacionId}/codigo-reserva`,
    )

    return respuesta.data
  },
}
