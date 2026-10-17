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
