package projects.dnetsova.taskmanager.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import projects.dnetsova.taskmanager.entities.Task;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    // Avoid N+1 when you return tasks and later serialize assignees
    @EntityGraph(attributePaths = "assignees")
    Page<Task> findAll(Specification<Task> spec, Pageable pageable);

    @Modifying
    @Transactional
    @Query("delete from Task t where t.id = :id or t.parentTaskId = :id")
    int deleteTaskAndDirectChildren(@Param("id") UUID id);
}

