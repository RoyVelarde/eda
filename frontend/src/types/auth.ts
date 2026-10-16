export interface LoginRequest {
  username: string
  password: string
}

export interface Usuario {
  id: string
  username: string
  nombres: string
  apellidos: string
  rol: 'CTS' | 'CS' | 'CAS'
  activo: boolean
}

export interface LoginResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresIn: number
  usuario: Usuario
}

export interface AuthSession {
  accessToken: string
  expiresAt: number
  usuario: Usuario
}
