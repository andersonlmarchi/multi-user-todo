<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const name = ref('')
const email = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref<string | null>(null)

const auth = useAuthStore()
const router = useRouter()

async function submit() {
  errorMsg.value = null
  if (!name.value.trim() || !email.value.trim() || !password.value) {
    errorMsg.value = 'Preencha todos os campos.'
    return
  }
  if (password.value.length < 8) {
    errorMsg.value = 'Senha deve ter no mínimo 8 caracteres.'
    return
  }
  loading.value = true
  try {
    await auth.register(name.value.trim(), email.value.trim(), password.value)
    await router.push('/app')
  } catch (e: unknown) {
    errorMsg.value = 'Não foi possível registrar. Email pode estar em uso.'
    console.error(e)
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
          <v-card-title class="text-h5 mb-4">Criar conta</v-card-title>
          <v-alert v-if="errorMsg" type="error" class="mb-4" density="compact">{{ errorMsg }}</v-alert>
          <v-form @submit.prevent="submit">
            <v-text-field v-model="name" label="Nome" autocomplete="name" required />
            <v-text-field v-model="email" label="Email" type="email" autocomplete="username" required />
            <v-text-field
              v-model="password"
              label="Senha"
              type="password"
              autocomplete="new-password"
              required
            />
            <v-btn type="submit" color="primary" block :loading="loading" class="mt-2">Registrar</v-btn>
            <v-btn variant="text" block class="mt-2" :to="{ name: 'login' }">Já tenho conta</v-btn>
          </v-form>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>
