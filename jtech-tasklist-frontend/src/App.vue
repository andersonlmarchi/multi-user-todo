<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { useTheme } from 'vuetify'

const theme = useTheme()
const darkMedia = window.matchMedia('(prefers-color-scheme: dark)')

function applyPreferredTheme(event?: MediaQueryListEvent) {
  const isDark = event?.matches ?? darkMedia.matches
  theme.global.name.value = isDark ? 'tasklistDark' : 'tasklistLight'
}

onMounted(() => {
  applyPreferredTheme()
  darkMedia.addEventListener('change', applyPreferredTheme)
})

onBeforeUnmount(() => {
  darkMedia.removeEventListener('change', applyPreferredTheme)
})
</script>

<template>
  <v-app>
    <RouterView />
  </v-app>
</template>
