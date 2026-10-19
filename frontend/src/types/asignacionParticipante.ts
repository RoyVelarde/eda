export interface AsignacionParticipanteRequest {
  dni: string
  codigoReserva?: string | null
}

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

export interface CambiarAsignacionParticipanteRequest {
  aulaCapacitacionId: string
  funcion: string
}

export interface CodigoReservaResponse {
  id: string
  capacitacionId: string
  activo: boolean
  fechaCreacion: string
  fechaActualizacion: string
}

export interface CodigoReservaRequest {
  codigo: string
}
