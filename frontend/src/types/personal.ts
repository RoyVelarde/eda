export type NivelPersonal = 'NIVEL_2' | 'NIVEL_3'

export interface Personal {
  id: string
  dni: string
  nivel: NivelPersonal
  apellidoPaterno: string
  apellidoMaterno: string
  nombres: string
  celular: string | null
  correo: string | null
  cargo: string
  situacion: string | null
  ranking: number | null
  fechaCreacion: string
}
