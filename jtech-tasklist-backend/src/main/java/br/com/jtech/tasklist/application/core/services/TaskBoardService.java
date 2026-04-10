package br.com.jtech.tasklist.application.core.services;

import br.com.jtech.tasklist.adapters.input.protocols.TaskCreateRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TaskListCreateRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TaskListResponse;
import br.com.jtech.tasklist.adapters.input.protocols.TaskListUpdateRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TaskResponse;
import br.com.jtech.tasklist.adapters.input.protocols.TaskUpdateRequest;
import br.com.jtech.tasklist.adapters.output.repositories.TaskEntityRepository;
import br.com.jtech.tasklist.adapters.output.repositories.TaskListEntityRepository;
import br.com.jtech.tasklist.adapters.output.repositories.UserEntityRepository;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TaskEntity;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TaskListEntity;
import br.com.jtech.tasklist.adapters.output.repositories.entities.UserEntity;
import br.com.jtech.tasklist.application.core.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskBoardService {

    private final TaskListEntityRepository taskListRepository;
    private final TaskEntityRepository taskRepository;
    private final UserEntityRepository userRepository;

    @Transactional(readOnly = true)
    public List<TaskListResponse> listTaskLists(UUID userId, boolean archived) {
        return taskListRepository.findByUser_IdAndArchivedOrderByCreatedAtDesc(userId, archived).stream()
                .map(t -> new TaskListResponse(t.getId(), t.getName(), t.isArchived()))
                .toList();
    }

    @Transactional
    public TaskListResponse createTaskList(UUID userId, TaskListCreateRequest req) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        TaskListEntity entity = TaskListEntity.builder()
                .id(UUID.randomUUID())
                .user(user)
                .name(req.name().trim())
                .archived(false)
                .createdAt(Instant.now())
                .build();
        taskListRepository.save(entity);
        return new TaskListResponse(entity.getId(), entity.getName(), entity.isArchived());
    }

    @Transactional(readOnly = true)
    public TaskListResponse getTaskList(UUID userId, UUID listId) {
        TaskListEntity t = taskListRepository.findByIdAndUser_Id(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task list not found"));
        return new TaskListResponse(t.getId(), t.getName(), t.isArchived());
    }

    @Transactional
    public TaskListResponse updateTaskList(UUID userId, UUID listId, TaskListUpdateRequest req) {
        TaskListEntity t = taskListRepository.findByIdAndUser_Id(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task list not found"));
        t.setName(req.name().trim());
        return new TaskListResponse(t.getId(), t.getName(), t.isArchived());
    }

    @Transactional
    public TaskListResponse archiveTaskList(UUID userId, UUID listId) {
        TaskListEntity t = taskListRepository.findByIdAndUser_Id(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task list not found"));
        t.setArchived(true);
        return new TaskListResponse(t.getId(), t.getName(), t.isArchived());
    }

    @Transactional
    public TaskListResponse unarchiveTaskList(UUID userId, UUID listId) {
        TaskListEntity t = taskListRepository.findByIdAndUser_Id(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task list not found"));
        t.setArchived(false);
        return new TaskListResponse(t.getId(), t.getName(), t.isArchived());
    }

    private TaskListEntity requireOwnedList(UUID userId, UUID listId) {
        return taskListRepository.findByIdAndUser_Id(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task list not found"));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(UUID userId, UUID listId, boolean archived) {
        requireOwnedList(userId, listId);
        return taskRepository.findByTaskList_IdAndArchivedOrderByCreatedAtAsc(listId, archived).stream()
                .map(t -> new TaskResponse(t.getId(), t.getTitle(), t.isDone(), t.isArchived()))
                .toList();
    }

    @Transactional
    public TaskResponse createTask(UUID userId, UUID listId, TaskCreateRequest req) {
        TaskListEntity list = requireOwnedList(userId, listId);
        TaskEntity task = TaskEntity.builder()
                .id(UUID.randomUUID())
                .taskList(list)
                .title(req.title().trim())
                .done(false)
                .archived(false)
                .createdAt(Instant.now())
                .build();
        taskRepository.save(task);
        return new TaskResponse(task.getId(), task.getTitle(), task.isDone(), task.isArchived());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(UUID userId, UUID listId, UUID taskId) {
        requireOwnedList(userId, listId);
        TaskEntity t = taskRepository.findByIdAndTaskList_Id(taskId, listId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return new TaskResponse(t.getId(), t.getTitle(), t.isDone(), t.isArchived());
    }

    @Transactional
    public TaskResponse updateTask(UUID userId, UUID listId, UUID taskId, TaskUpdateRequest req) {
        requireOwnedList(userId, listId);
        TaskEntity t = taskRepository.findByIdAndTaskList_Id(taskId, listId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (req.title() != null) {
            t.setTitle(req.title().trim());
        }
        if (req.done() != null) {
            t.setDone(req.done());
        }
        return new TaskResponse(t.getId(), t.getTitle(), t.isDone(), t.isArchived());
    }

    @Transactional
    public TaskResponse archiveTask(UUID userId, UUID listId, UUID taskId) {
        requireOwnedList(userId, listId);
        TaskEntity t = taskRepository.findByIdAndTaskList_Id(taskId, listId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        t.setArchived(true);
        return new TaskResponse(t.getId(), t.getTitle(), t.isDone(), t.isArchived());
    }

    @Transactional
    public TaskResponse unarchiveTask(UUID userId, UUID listId, UUID taskId) {
        requireOwnedList(userId, listId);
        TaskEntity t = taskRepository.findByIdAndTaskList_Id(taskId, listId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        t.setArchived(false);
        return new TaskResponse(t.getId(), t.getTitle(), t.isDone(), t.isArchived());
    }
}
