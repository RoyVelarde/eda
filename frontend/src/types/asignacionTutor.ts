export type FuncionTutor = 'RESPONSABLE' | 'APOYO'

export interface AsignacionTutor {
  id: string
  capacitacionId: string
  aulaCapacitacionId: string
  personalId: string
  dni: string
  apellidoPaterno: string
  apellidoMaterno: string
  nombres: string
  cargo: string
  funcion: FuncionTutor
  activa: boolean
  fechaAsignacion: string
}

export interface AsignacionTutorRequest {
  personalId: string
  funcion: FuncionTutor
}

export interface ActualizarAsignacionTutorRequest {
  aulaCapacitacionId: string
  funcion: FuncionTutor
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
