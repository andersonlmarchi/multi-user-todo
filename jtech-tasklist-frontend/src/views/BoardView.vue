<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'

const auth = useAuthStore()
const app = useAppStore()
const router = useRouter()

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
  <v-app-bar color="primary" prominent>
    <v-app-bar-title>Minhas listas</v-app-bar-title>
    <v-spacer />
    <span class="mr-4 text-caption">{{ auth.userEmail }}</span>
    <v-btn variant="text" @click="logout">Sair</v-btn>
  </v-app-bar>

  <v-navigation-drawer permanent width="280">
    <v-list density="compact">
      <v-list-subheader>
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
      <v-btn block class="ma-2" size="small" color="secondary" @click="openNewList">Nova lista</v-btn>
      <v-list-item
        v-for="list in app.lists"
        :key="list.id"
        :active="app.selectedListId === list.id"
        :title="list.name"
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
    <v-container fluid>
      <v-row v-if="!app.selectedListId" align="center" justify="center">
        <v-col cols="12" class="text-center text-medium-emphasis">Selecione ou crie uma lista.</v-col>
      </v-row>
      <template v-else>
        <v-row align="center" class="mb-2">
          <v-col cols="auto">
            <h2 class="text-h6">
              {{ app.lists.find((l) => l.id === app.selectedListId)?.name }}
            </h2>
          </v-col>
          <v-col cols="auto">
            <v-switch v-model="app.taskArchivedView" hide-details inset label="Tarefas arquivadas" />
          </v-col>
          <v-spacer />
          <v-col cols="auto">
            <v-btn color="primary" @click="openNewTask">Nova tarefa</v-btn>
          </v-col>
        </v-row>
        <v-list lines="three">
          <v-list-item v-for="task in app.tasks" :key="task.id">
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
            <v-list-item-title>{{ task.title }}</v-list-item-title>
            <template #append>
              <v-btn
                v-if="!task.archived"
                icon="mdi-archive-outline"
                variant="text"
                @click="askArchiveTask(task.id)"
              />
              <v-btn v-else icon="mdi-archive-off-outline" variant="text" @click="unarchiveTask(task.id)" />
            </template>
          </v-list-item>
        </v-list>
      </template>
    </v-container>
  </v-main>

  <v-dialog v-model="dialogNewList" max-width="400">
    <v-card>
      <v-card-title>Nova lista</v-card-title>
      <v-card-text>
        <v-text-field v-model="newListName" label="Nome" @keyup.enter="submitNewList" />
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn @click="dialogNewList = false">Cancelar</v-btn>
        <v-btn color="primary" @click="submitNewList">Criar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="dialogRenameList" max-width="400">
    <v-card>
      <v-card-title>Renomear lista</v-card-title>
      <v-card-text>
        <v-text-field v-model="renameListName" label="Nome" @keyup.enter="submitRename" />
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn @click="dialogRenameList = false">Cancelar</v-btn>
        <v-btn color="primary" @click="submitRename">Salvar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="dialogNewTask" max-width="400">
    <v-card>
      <v-card-title>Nova tarefa</v-card-title>
      <v-card-text>
        <v-text-field v-model="newTaskTitle" label="Título" @keyup.enter="submitNewTask" />
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn @click="dialogNewTask = false">Cancelar</v-btn>
        <v-btn color="primary" @click="submitNewTask">Adicionar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="confirmArchiveList" max-width="400">
    <v-card>
      <v-card-title>Arquivar lista?</v-card-title>
      <v-card-text>As tarefas permanecem na lista arquivada. Você pode desarquivar depois.</v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn @click="confirmArchiveList = false">Cancelar</v-btn>
        <v-btn color="warning" @click="doArchiveList">Arquivar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="confirmArchiveTask" max-width="400">
    <v-card>
      <v-card-title>Arquivar tarefa?</v-card-title>
      <v-card-actions>
        <v-spacer />
        <v-btn @click="confirmArchiveTask = false">Cancelar</v-btn>
        <v-btn color="warning" @click="doArchiveTask">Arquivar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
