export type TipoAula = 'APLICADOR' | 'ORIENTADOR'

export interface Aula {
  id: string
  codigo: string
  tipo: TipoAula
  capacidad: number
  activa: boolean
  fechaCreacion: string
}

export interface AulaRequest {
  codigo: string
  tipo: TipoAula
  capacidad: number
}

export interface AulaCapacitacion {
  id: string
  capacitacionId: string
  aulaId: string
  codigoAula: string
  tipoAula: string
  capacidad: number
  activa: boolean
  fechaAsignacion: string
}

export interface AulaCapacitacionRequest {
  aulaId: string
}
