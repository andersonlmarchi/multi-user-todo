import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import api from '@/services/api'
import type { AuthTokenResponse } from '@/types/api'

export const useAuthStore = defineStore(
  'auth',
  () => {
    const accessToken = ref<string | null>(null)
    const refreshToken = ref<string | null>(null)
    const userEmail = ref<string | null>(null)

    const isAuthenticated = computed(() => !!accessToken.value)

    function setTokens(at: string, rt: string) {
      accessToken.value = at
      refreshToken.value = rt
    }

    function clear() {
      accessToken.value = null
      refreshToken.value = null
      userEmail.value = null
    }

    async function register(name: string, email: string, password: string) {
      const { data } = await api.post<AuthTokenResponse>('/api/v1/auth/register', {
        name,
        email,
        password,
      })
      setTokens(data.accessToken, data.refreshToken)
      userEmail.value = email
    }

    async function login(email: string, password: string) {
      const { data } = await api.post<AuthTokenResponse>('/api/v1/auth/login', { email, password })
      setTokens(data.accessToken, data.refreshToken)
      userEmail.value = email
    }

    function logout() {
      clear()
    }

    return {
      accessToken,
      refreshToken,
      userEmail,
      isAuthenticated,
      setTokens,
      clear,
      register,
      login,
      logout,
    }
  },
  {
    persist: {
      pick: ['accessToken', 'refreshToken', 'userEmail'],
    },
  },
)
