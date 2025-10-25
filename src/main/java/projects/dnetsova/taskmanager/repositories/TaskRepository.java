package projects.dnetsova.taskmanager.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projects.dnetsova.taskmanager.entities.Task;
import projects.dnetsova.taskmanager.utils.Priority;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("""
        SELECT t
        FROM Task t
        LEFT JOIN t.assignees a
        WHERE (:priority IS NULL OR t.priority = :priority)
          AND (:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:deadline IS NULL OR t.deadline <= :deadline)
          AND (:assigneesSize IS NULL OR :assigneesSize = 0 OR a.name IN :assignees)
        GROUP BY t
        HAVING (:assigneesSize IS NULL OR :assigneesSize = 0
                OR COUNT(DISTINCT a.name) = :assigneesSize)
        ORDER BY t.priority ASC, t.deadline ASC NULLS LAST
    """)
    Page<Task> getTasksFiltered(
            @Param("priority") Priority priority,
            @Param("title") String title,
            @Param("deadline") LocalDate deadline,
            @Param("assignees") Set<String> assignees,
            @Param("assigneesSize") Long assigneesSize,
            Pageable pageable
    );
}
