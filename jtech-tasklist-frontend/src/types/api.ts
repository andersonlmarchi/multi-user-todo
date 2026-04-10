export interface AuthTokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
}

export interface TaskListResponse {
  id: string
  name: string
  archived: boolean
}

export interface TaskResponse {
  id: string
  title: string
  done: boolean
  archived: boolean
}
