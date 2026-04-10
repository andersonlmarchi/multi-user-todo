import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/services/api'
import type { TaskListResponse, TaskResponse } from '@/types/api'

export const useAppStore = defineStore(
  'app',
  () => {
    const lists = ref<TaskListResponse[]>([])
    const listArchivedView = ref(false)
    const taskArchivedView = ref(false)
    const selectedListId = ref<string | null>(null)
    const tasks = ref<TaskResponse[]>([])

    async function loadLists(archived: boolean) {
      listArchivedView.value = archived
      const { data } = await api.get<TaskListResponse[]>('/api/v1/task-lists', {
        params: { archived },
      })
      lists.value = data
      if (!selectedListId.value && data.length > 0) {
        selectedListId.value = data[0].id
        await loadTasks()
      } else if (selectedListId.value && !data.some((l) => l.id === selectedListId.value)) {
        selectedListId.value = data[0]?.id ?? null
        tasks.value = []
        if (selectedListId.value) await loadTasks()
      }
    }

    async function loadTasks() {
      if (!selectedListId.value) {
        tasks.value = []
        return
      }
      const { data } = await api.get<TaskResponse[]>(
        `/api/v1/task-lists/${selectedListId.value}/tasks`,
        { params: { archived: taskArchivedView.value } },
      )
      tasks.value = data
    }

    function selectList(id: string) {
      selectedListId.value = id
    }

    async function createList(name: string) {
      const { data } = await api.post<TaskListResponse>('/api/v1/task-lists', { name })
      await loadLists(listArchivedView.value)
      selectedListId.value = data.id
      await loadTasks()
    }

    async function renameList(id: string, name: string) {
      await api.put(`/api/v1/task-lists/${id}`, { name })
      await loadLists(listArchivedView.value)
    }

    async function archiveList(id: string) {
      await api.post(`/api/v1/task-lists/${id}/archive`)
      await loadLists(listArchivedView.value)
      await loadTasks()
    }

    async function unarchiveList(id: string) {
      await api.post(`/api/v1/task-lists/${id}/unarchive`)
      await loadLists(listArchivedView.value)
      await loadTasks()
    }

    async function createTask(title: string) {
      if (!selectedListId.value) return
      await api.post(`/api/v1/task-lists/${selectedListId.value}/tasks`, { title })
      await loadTasks()
    }

    async function updateTask(taskId: string, patch: { title?: string; done?: boolean }) {
      if (!selectedListId.value) return
      await api.put(`/api/v1/task-lists/${selectedListId.value}/tasks/${taskId}`, patch)
      await loadTasks()
    }

    async function archiveTask(taskId: string) {
      if (!selectedListId.value) return
      await api.post(`/api/v1/task-lists/${selectedListId.value}/tasks/${taskId}/archive`)
      await loadTasks()
    }

    async function unarchiveTask(taskId: string) {
      if (!selectedListId.value) return
      await api.post(`/api/v1/task-lists/${selectedListId.value}/tasks/${taskId}/unarchive`)
      await loadTasks()
    }

    return {
      lists,
      listArchivedView,
      taskArchivedView,
      selectedListId,
      tasks,
      loadLists,
      loadTasks,
      selectList,
      createList,
      renameList,
      archiveList,
      unarchiveList,
      createTask,
      updateTask,
      archiveTask,
      unarchiveTask,
    }
  },
  {
    persist: {
      pick: ['lists', 'selectedListId', 'tasks', 'listArchivedView', 'taskArchivedView'],
    },
  },
)
