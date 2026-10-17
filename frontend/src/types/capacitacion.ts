export type EstadoCapacitacion = 'PLANIFICADA' | 'EN_CURSO' | 'FINALIZADA' | 'CANCELADA'

export interface Capacitacion {
  id: string
  nombre: string
  anio: number
  fechaInicio: string
  fechaFin: string
  estado: EstadoCapacitacion
  fechaCreacion: string
  fechaActualizacion: string
}

export interface CapacitacionRequest {
  nombre: string
  anio: number
  fechaInicio: string
  fechaFin: string
}
