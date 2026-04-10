package br.com.jtech.tasklist.adapters.input.controllers;

import br.com.jtech.tasklist.adapters.input.protocols.TaskCreateRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TaskListCreateRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TaskListResponse;
import br.com.jtech.tasklist.adapters.input.protocols.TaskListUpdateRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TaskResponse;
import br.com.jtech.tasklist.adapters.input.protocols.TaskUpdateRequest;
import br.com.jtech.tasklist.application.core.services.TaskBoardService;
import br.com.jtech.tasklist.config.security.CurrentUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/task-lists")
@RequiredArgsConstructor
public class TaskListController {

    private final TaskBoardService taskBoardService;

    @GetMapping
    public List<TaskListResponse> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "false") boolean archived) {
        UUID userId = CurrentUserId.from(jwt);
        return taskBoardService.listTaskLists(userId, archived);
    }

    @PostMapping
    public ResponseEntity<TaskListResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TaskListCreateRequest request) {
        UUID userId = CurrentUserId.from(jwt);
        TaskListResponse body = taskBoardService.createTaskList(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{listId}")
    public TaskListResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID listId) {
        return taskBoardService.getTaskList(CurrentUserId.from(jwt), listId);
    }

    @PutMapping("/{listId}")
    public TaskListResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID listId,
            @Valid @RequestBody TaskListUpdateRequest request) {
        return taskBoardService.updateTaskList(CurrentUserId.from(jwt), listId, request);
    }

    @PostMapping("/{listId}/archive")
    public TaskListResponse archive(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID listId) {
        return taskBoardService.archiveTaskList(CurrentUserId.from(jwt), listId);
    }

    @PostMapping("/{listId}/unarchive")
    public TaskListResponse unarchive(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID listId) {
        return taskBoardService.unarchiveTaskList(CurrentUserId.from(jwt), listId);
    }

    @GetMapping("/{listId}/tasks")
    public List<TaskResponse> listTasks(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID listId,
            @RequestParam(defaultValue = "false") boolean archived) {
        return taskBoardService.listTasks(CurrentUserId.from(jwt), listId, archived);
    }

    @PostMapping("/{listId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID listId,
            @Valid @RequestBody TaskCreateRequest request) {
        TaskResponse body = taskBoardService.createTask(CurrentUserId.from(jwt), listId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{listId}/tasks/{taskId}")
    public TaskResponse getTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID listId,
            @PathVariable UUID taskId) {
        return taskBoardService.getTask(CurrentUserId.from(jwt), listId, taskId);
    }

    @PutMapping("/{listId}/tasks/{taskId}")
    public TaskResponse updateTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID listId,
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskUpdateRequest request) {
        return taskBoardService.updateTask(CurrentUserId.from(jwt), listId, taskId, request);
    }

    @PostMapping("/{listId}/tasks/{taskId}/archive")
    public TaskResponse archiveTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID listId,
            @PathVariable UUID taskId) {
        return taskBoardService.archiveTask(CurrentUserId.from(jwt), listId, taskId);
    }

    @PostMapping("/{listId}/tasks/{taskId}/unarchive")
    public TaskResponse unarchiveTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID listId,
            @PathVariable UUID taskId) {
        return taskBoardService.unarchiveTask(CurrentUserId.from(jwt), listId, taskId);
    }
}
