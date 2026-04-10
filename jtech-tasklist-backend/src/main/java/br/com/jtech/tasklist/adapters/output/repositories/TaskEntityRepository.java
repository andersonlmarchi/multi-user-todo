package br.com.jtech.tasklist.adapters.output.repositories;

import br.com.jtech.tasklist.adapters.output.repositories.entities.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskEntityRepository extends JpaRepository<TaskEntity, UUID> {

    List<TaskEntity> findByTaskList_IdAndArchivedOrderByCreatedAtAsc(UUID taskListId, boolean archived);

    Optional<TaskEntity> findByIdAndTaskList_Id(UUID id, UUID taskListId);
}
