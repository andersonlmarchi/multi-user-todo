import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/auth'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export const api = axios.create({ baseURL })

function authPath(url: string | undefined) {
  return url?.includes('/auth/')
}

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  if (authPath(config.url)) {
    return config
  }
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

let refreshInFlight: Promise<string | null> | null = null

async function refreshAccessToken(): Promise<string | null> {
  const auth = useAuthStore()
  if (!auth.refreshToken) {
    return null
  }
  if (!refreshInFlight) {
    refreshInFlight = axios
      .post<{ accessToken: string; refreshToken: string }>(`${baseURL}/api/v1/auth/refresh`, {
        refreshToken: auth.refreshToken,
      })
      .then(({ data }) => {
        auth.setTokens(data.accessToken, data.refreshToken)
        return data.accessToken
      })
      .catch(() => {
        auth.clear()
        return null
      })
      .finally(() => {
        refreshInFlight = null
      })
  }
  return refreshInFlight
}

api.interceptors.response.use(
  (res) => res,
  async (error: AxiosError) => {
    const original = error.config as InternalAxiosRequestConfig & { _retry?: boolean }
    if (error.response?.status !== 401 || !original || authPath(original.url)) {
      return Promise.reject(error)
    }
    if (original._retry) {
      return Promise.reject(error)
    }
    original._retry = true
    const newAccess = await refreshAccessToken()
    if (!newAccess) {
      return Promise.reject(error)
    }
    original.headers.Authorization = `Bearer ${newAccess}`
    return api(original)
  },
)

export default api
