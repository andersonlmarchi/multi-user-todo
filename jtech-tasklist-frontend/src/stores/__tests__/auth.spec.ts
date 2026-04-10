import { createPinia, setActivePinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import api from '@/services/api'
import { useAuthStore } from '../auth'

vi.mock('@/services/api', () => ({
  default: {
    post: vi.fn(),
  },
}))

function freshPinia() {
  const pinia = createPinia()
  pinia.use(piniaPluginPersistedstate)
  return pinia
}

describe('authStore', () => {
  beforeEach(() => {
    setActivePinia(freshPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('login stores tokens and email', async () => {
    vi.mocked(api.post).mockResolvedValue({
      data: {
        accessToken: 'a',
        refreshToken: 'r',
        tokenType: 'Bearer',
        expiresIn: 900,
      },
    })
    const auth = useAuthStore()
    await auth.login('u@x.com', 'secretpass')
    expect(auth.accessToken).toBe('a')
    expect(auth.refreshToken).toBe('r')
    expect(auth.userEmail).toBe('u@x.com')
    expect(api.post).toHaveBeenCalledWith('/api/v1/auth/login', {
      email: 'u@x.com',
      password: 'secretpass',
    })
  })

  it('logout clears session', async () => {
    vi.mocked(api.post).mockResolvedValue({
      data: { accessToken: 'a', refreshToken: 'r', tokenType: 'Bearer', expiresIn: 1 },
    })
    const auth = useAuthStore()
    await auth.login('u@x.com', 'p')
    auth.logout()
    expect(auth.accessToken).toBeNull()
    expect(auth.isAuthenticated).toBe(false)
  })
})
