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
  <v-container class="fill-height auth-page px-3 px-sm-4" max-width="520">
    <v-row justify="center" align="center">
      <v-col cols="12">
        <v-card class="auth-card pa-4 pa-sm-8">
          <div class="d-flex align-center mb-6 header-block">
            <v-avatar color="secondary" variant="tonal" class="mr-3" size="40">
              <v-icon icon="mdi-account-plus-outline" />
            </v-avatar>
            <div class="d-flex flex-column ga-1">
              <div class="text-overline text-secondary font-weight-bold">TASKLIST</div>
              <v-card-title class="text-h5 pa-0">Criar conta</v-card-title>
            </div>
          </div>
          <v-alert v-if="errorMsg" type="error" class="mb-4" density="compact">{{ errorMsg }}</v-alert>
          <v-form @submit.prevent="submit" class="d-flex flex-column ga-2">
            <v-text-field v-model="name" label="Nome" autocomplete="name" required />
            <v-text-field v-model="email" label="Email" type="email" autocomplete="username" required />
            <v-text-field
              v-model="password"
              label="Senha"
              type="password"
              autocomplete="new-password"
              required
            />
            <v-btn type="submit" color="primary" size="large" block :loading="loading" class="mt-3">
              Registrar
            </v-btn>
            <v-btn variant="tonal" color="secondary" block class="mt-2" :to="{ name: 'login' }">
              Já tenho conta
            </v-btn>
          </v-form>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
.auth-page {
  min-height: 100dvh;
  display: flex;
  align-items: center;
}

.header-block {
  padding-bottom: 0.25rem;
}

.auth-card {
  border: 1px solid rgba(var(--v-theme-outline), 0.5);
  box-shadow: var(--shadow-md);
}
</style>
