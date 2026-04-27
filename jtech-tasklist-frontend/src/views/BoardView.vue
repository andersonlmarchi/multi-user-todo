<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useDisplay } from 'vuetify'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'

const auth = useAuthStore()
const app = useAppStore()
const router = useRouter()
const display = useDisplay()
const drawerOpen = ref(false)
const isMobile = computed(() => display.mdAndDown.value)
const drawerVisible = computed({
  get: () => (isMobile.value ? drawerOpen.value : true),
  set: (value: boolean) => {
    if (isMobile.value) {
      drawerOpen.value = value
    }
  },
})
const selectedListName = computed(
  () => app.lists.find((list) => list.id === app.selectedListId)?.name ?? 'Lista',
)
const visibleTasksCount = computed(() => app.tasks.length)
const doneTasksCount = computed(() => app.tasks.filter((task) => task.done && !task.archived).length)

const dialogNewList = ref(false)
const dialogRenameList = ref(false)
const newListName = ref('')
const renameListName = ref('')
const renamingListId = ref<string | null>(null)

const dialogNewTask = ref(false)
const newTaskTitle = ref('')

const confirmArchiveList = ref(false)
const listToArchive = ref<string | null>(null)
const confirmArchiveTask = ref(false)
const taskToArchive = ref<string | null>(null)

onMounted(async () => {
  await app.loadLists(app.listArchivedView)
  if (app.selectedListId) {
    await app.loadTasks()
  }
})

watch(
  () => app.selectedListId,
  async () => {
    await app.loadTasks()
  },
)

watch(
  () => app.listArchivedView,
  async () => {
    await app.loadLists(app.listArchivedView)
  },
)

watch(
  () => app.taskArchivedView,
  async () => {
    await app.loadTasks()
  },
)

function logout() {
  auth.logout()
  router.push({ name: 'login' })
}

async function openNewList() {
  newListName.value = ''
  dialogNewList.value = true
}

async function submitNewList() {
  if (!newListName.value.trim()) return
  await app.createList(newListName.value.trim())
  dialogNewList.value = false
}

function openRename(listId: string, current: string) {
  renamingListId.value = listId
  renameListName.value = current
  dialogRenameList.value = true
}

async function submitRename() {
  if (!renamingListId.value || !renameListName.value.trim()) return
  await app.renameList(renamingListId.value, renameListName.value.trim())
  dialogRenameList.value = false
}

function askArchiveList(id: string) {
  listToArchive.value = id
  confirmArchiveList.value = true
}

async function doArchiveList() {
  if (listToArchive.value) {
    await app.archiveList(listToArchive.value)
  }
  confirmArchiveList.value = false
}

async function unarchiveList(id: string) {
  await app.unarchiveList(id)
}

async function select(id: string) {
  app.selectList(id)
  if (isMobile.value) {
    drawerOpen.value = false
  }
}

async function openNewTask() {
  newTaskTitle.value = ''
  dialogNewTask.value = true
}

async function submitNewTask() {
  if (!newTaskTitle.value.trim()) return
  await app.createTask(newTaskTitle.value.trim())
  dialogNewTask.value = false
}

function askArchiveTask(id: string) {
  taskToArchive.value = id
  confirmArchiveTask.value = true
}

async function doArchiveTask() {
  if (taskToArchive.value) {
    await app.archiveTask(taskToArchive.value)
  }
  confirmArchiveTask.value = false
}

async function unarchiveTask(id: string) {
  await app.unarchiveTask(id)
}
</script>

<template>
  <v-app-bar color="surface" class="app-header px-2 px-sm-4" elevation="0">
    <v-app-bar-nav-icon v-if="isMobile" @click="drawerOpen = !drawerOpen" />
    <v-app-bar-title class="font-weight-bold text-truncate pr-2">Minhas listas</v-app-bar-title>
    <v-spacer />
    <v-menu location="bottom end">
      <template #activator="{ props }">
        <v-btn v-bind="props" variant="text" append-icon="mdi-chevron-down" class="email-menu-btn">
          <span class="text-caption text-none text-truncate">{{ auth.userEmail }}</span>
        </v-btn>
      </template>
      <v-list class="py-2" min-width="180">
        <v-list-item>
          <v-list-item-title class="text-caption text-medium-emphasis">Conta conectada</v-list-item-title>
          <v-list-item-subtitle class="text-body-2 text-truncate">{{ auth.userEmail }}</v-list-item-subtitle>
        </v-list-item>
        <v-divider class="my-2" />
        <v-list-item prepend-icon="mdi-logout" title="Sair" @click="logout" />
      </v-list>
    </v-menu>
  </v-app-bar>

  <v-navigation-drawer
    v-model="drawerVisible"
    :permanent="!isMobile"
    :temporary="isMobile"
    :location="isMobile ? 'start' : undefined"
    width="280"
    class="app-drawer"
  >
    <div class="px-4 pt-4 pb-2 d-md-none">
      <div class="text-subtitle-2">Conta conectada</div>
      <div class="text-caption text-medium-emphasis text-truncate">{{ auth.userEmail }}</div>
    </div>
    <v-list density="compact" class="pt-3 px-2 drawer-list">
      <v-list-subheader class="font-weight-medium">
        <span>Listas</span>
        <v-spacer />
        <v-switch
          v-model="app.listArchivedView"
          hide-details
          density="compact"
          inset
          label="Arquivadas"
          class="ml-2"
        />
      </v-list-subheader>
      <v-btn block class="mx-3 mt-3 mb-4" color="secondary" prepend-icon="mdi-plus" @click="openNewList">
        Nova lista
      </v-btn>
      <v-list-item
        v-for="list in app.lists"
        :key="list.id"
        :active="app.selectedListId === list.id"
        :title="list.name"
        class="mx-2 mb-2 px-2 py-1 rounded-lg"
        @click="select(list.id)"
      >
        <template #append>
          <v-menu>
            <template #activator="{ props: menuProps }">
              <v-btn icon="mdi-dots-vertical" variant="text" v-bind="menuProps" />
            </template>
            <v-list>
              <v-list-item title="Renomear" @click="openRename(list.id, list.name)" />
              <v-list-item v-if="!list.archived" title="Arquivar" @click="askArchiveList(list.id)" />
              <v-list-item v-else title="Desarquivar" @click="unarchiveList(list.id)" />
            </v-list>
          </v-menu>
        </template>
      </v-list-item>
    </v-list>
  </v-navigation-drawer>

  <v-main>
    <v-container fluid class="pa-4 pa-sm-6">
      <v-row v-if="!app.selectedListId" align="center" justify="center">
        <v-col cols="12" md="6">
          <v-card class="empty-state pa-5 pa-sm-8 text-center">
            <v-icon icon="mdi-view-dashboard-outline" size="46" color="primary" class="mb-3" />
            <div class="text-h6 mb-1">Escolha uma lista para começar</div>
            <div class="text-body-2 text-medium-emphasis mb-4">
              Crie uma nova lista ou selecione uma existente no menu lateral.
            </div>
            <v-btn color="primary" prepend-icon="mdi-plus" @click="openNewList">Nova lista</v-btn>
          </v-card>
        </v-col>
      </v-row>
      <template v-else>
        <v-row align="center" class="mb-4">
          <v-col cols="12" sm="auto" class="pb-1 pb-sm-3">
            <div class="text-h6 font-weight-bold">{{ selectedListName }}</div>
            <div class="text-caption text-medium-emphasis">
              {{ visibleTasksCount }} tarefa(s) visível(is) • {{ doneTasksCount }} concluída(s)
            </div>
          </v-col>
          <v-col cols="12" sm="auto">
            <v-switch
              v-model="app.taskArchivedView"
              hide-details
              inset
              label="Tarefas arquivadas"
              class="mt-n2 mt-sm-0"
            />
          </v-col>
          <v-spacer class="d-none d-sm-block" />
          <v-col cols="12" sm="auto">
            <v-btn color="primary" prepend-icon="mdi-plus" :block="isMobile" @click="openNewTask">
              Nova tarefa
            </v-btn>
          </v-col>
        </v-row>
        <v-card variant="flat" class="board-card">
          <v-list lines="three" class="bg-transparent px-2 py-2">
            <template v-if="app.tasks.length">
              <v-list-item v-for="task in app.tasks" :key="task.id" class="task-item mb-3 px-2 py-1 rounded-lg">
                <template #prepend>
                  <v-checkbox
                    v-if="!task.archived"
                    :model-value="task.done"
                    hide-details
                    @update:model-value="
                      (v: boolean | null) => app.updateTask(task.id, { done: Boolean(v) })
                    "
                  />
                </template>
                <v-list-item-title :class="{ 'text-decoration-line-through text-medium-emphasis': task.done }">
                  {{ task.title }}
                </v-list-item-title>
                <template #append>
                  <v-btn
                    v-if="!task.archived"
                    icon="mdi-archive-outline"
                    variant="tonal"
                    size="small"
                    @click="askArchiveTask(task.id)"
                  />
                  <v-btn
                    v-else
                    icon="mdi-archive-off-outline"
                    variant="tonal"
                    size="small"
                    @click="unarchiveTask(task.id)"
                  />
                </template>
              </v-list-item>
            </template>
            <div v-else class="py-14 px-4 text-center text-medium-emphasis">
              <v-icon icon="mdi-format-list-checkbox" size="42" class="mb-2" />
              <div class="text-subtitle-1">Nenhuma tarefa por aqui</div>
              <div class="text-body-2">Crie uma nova tarefa para começar.</div>
            </div>
          </v-list>
        </v-card>
      </template>
    </v-container>
  </v-main>

  <v-dialog v-model="dialogNewList" max-width="400">
    <v-card class="dialog-card">
      <v-card-title>Nova lista</v-card-title>
      <v-card-text class="pt-2 pb-1">
        <v-text-field v-model="newListName" label="Nome" @keyup.enter="submitNewList" />
      </v-card-text>
      <v-card-actions class="px-5 pb-5">
        <v-spacer />
        <v-btn @click="dialogNewList = false">Cancelar</v-btn>
        <v-btn color="primary" @click="submitNewList">Criar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="dialogRenameList" max-width="400">
    <v-card class="dialog-card">
      <v-card-title>Renomear lista</v-card-title>
      <v-card-text class="pt-2 pb-1">
        <v-text-field v-model="renameListName" label="Nome" @keyup.enter="submitRename" />
      </v-card-text>
      <v-card-actions class="px-5 pb-5">
        <v-spacer />
        <v-btn @click="dialogRenameList = false">Cancelar</v-btn>
        <v-btn color="primary" @click="submitRename">Salvar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="dialogNewTask" max-width="400">
    <v-card class="dialog-card">
      <v-card-title>Nova tarefa</v-card-title>
      <v-card-text class="pt-2 pb-1">
        <v-text-field v-model="newTaskTitle" label="Título" @keyup.enter="submitNewTask" />
      </v-card-text>
      <v-card-actions class="px-5 pb-5">
        <v-spacer />
        <v-btn @click="dialogNewTask = false">Cancelar</v-btn>
        <v-btn color="primary" @click="submitNewTask">Adicionar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="confirmArchiveList" max-width="400">
    <v-card class="dialog-card">
      <v-card-title>Arquivar lista?</v-card-title>
      <v-card-text class="pt-2 pb-1">
        As tarefas permanecem na lista arquivada. Você pode desarquivar depois.
      </v-card-text>
      <v-card-actions class="px-5 pb-5">
        <v-spacer />
        <v-btn @click="confirmArchiveList = false">Cancelar</v-btn>
        <v-btn color="warning" @click="doArchiveList">Arquivar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="confirmArchiveTask" max-width="400">
    <v-card class="dialog-card">
      <v-card-title>Arquivar tarefa?</v-card-title>
      <v-card-actions class="px-5 pb-5">
        <v-spacer />
        <v-btn @click="confirmArchiveTask = false">Cancelar</v-btn>
        <v-btn color="warning" @click="doArchiveTask">Arquivar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.app-header {
  border-bottom: 1px solid rgba(var(--v-theme-outline), 0.5);
  backdrop-filter: blur(12px);
}

.app-drawer {
  border-right: 1px solid rgba(var(--v-theme-outline), 0.5);
}

.email-menu-btn {
  max-width: min(42vw, 220px);
  margin-right: 0.25rem;
}

.drawer-list :deep(.v-list-item) {
  margin-bottom: 0.25rem;
}

.drawer-list :deep(.v-list-item:last-child) {
  margin-bottom: 0;
}

.board-card {
  background: rgb(var(--v-theme-surface));
  border: 1px solid rgba(var(--v-theme-outline), 0.5);
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
  padding: 0.75rem;
}

.task-item {
  border: 1px solid rgba(var(--v-theme-outline), 0.45);
  background: color-mix(in srgb, rgb(var(--v-theme-surface)) 86%, rgb(var(--v-theme-primary)) 14%);
  transition: transform 160ms ease-out, box-shadow 160ms ease-out;
}

.task-item:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.dialog-card {
  border: 1px solid rgba(var(--v-theme-outline), 0.55);
}

.empty-state {
  border: 1px solid rgba(var(--v-theme-outline), 0.5);
  box-shadow: var(--shadow-sm);
}
</style>
