package br.com.jtech.tasklist.adapters.output.repositories;

import br.com.jtech.tasklist.adapters.output.repositories.entities.TaskListEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskListEntityRepository extends JpaRepository<TaskListEntity, UUID> {

    List<TaskListEntity> findByUser_IdAndArchivedOrderByCreatedAtDesc(UUID userId, boolean archived);

    Optional<TaskListEntity> findByIdAndUser_Id(UUID id, UUID userId);
}
