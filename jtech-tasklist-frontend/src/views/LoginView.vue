<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const email = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref<string | null>(null)

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

async function submit() {
  errorMsg.value = null
  if (!email.value.trim() || !password.value) {
    errorMsg.value = 'Preencha email e senha.'
    return
  }
  loading.value = true
  try {
    await auth.login(email.value.trim(), password.value)
    const redirect = (route.query.redirect as string) || '/app'
    await router.push(redirect)
  } catch {
    errorMsg.value = 'Credenciais inválidas.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <v-container class="fill-height" max-width="480">
    <v-row justify="center" align="center">
      <v-col cols="12">
        <v-card class="pa-6">
          <v-card-title class="text-h5 mb-4">Entrar</v-card-title>
          <v-alert v-if="errorMsg" type="error" class="mb-4" density="compact">{{ errorMsg }}</v-alert>
          <v-form @submit.prevent="submit">
            <v-text-field v-model="email" label="Email" type="email" autocomplete="username" required />
            <v-text-field
              v-model="password"
              label="Senha"
              type="password"
              autocomplete="current-password"
              required
            />
            <v-btn type="submit" color="primary" block :loading="loading" class="mt-2">Login</v-btn>
            <v-btn variant="text" block class="mt-2" :to="{ name: 'register' }">Criar conta</v-btn>
          </v-form>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>
