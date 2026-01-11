package projects.dnetsova.taskmanager.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import projects.dnetsova.taskmanager.entities.Task;
import projects.dnetsova.taskmanager.utils.Priority;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query(value = """
                SELECT t
                FROM Task t
                WHERE (:priority IS NULL OR t.priority = :priority)
                  AND (:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))
                  AND (:deadline IS NULL OR t.deadline <= :deadline)
                  AND (:isCompleted IS NULL OR t.isCompleted = :isCompleted)
                  AND (
                    (:parentTaskId IS NULL AND t.parentTaskId IS NULL)
                    OR (:parentTaskId IS NOT NULL AND t.parentTaskId = :parentTaskId)
                  )
                  AND (
                    :assigneesSize IS NULL OR :assigneesSize = 0 OR
                    (SELECT COUNT(DISTINCT a.name)
                     FROM t.assignees a
                     WHERE a.name IN :assignees
                    ) = :assigneesSize
                  )
                ORDER BY
                  t.priority ASC,
                  CASE WHEN t.deadline IS NULL THEN 1 ELSE 0 END ASC,
                  t.deadline ASC
            """,
            countQuery = """
                        SELECT COUNT(t)
                        FROM Task t
                        WHERE (:priority IS NULL OR t.priority = :priority)
                          AND (:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))
                          AND (:deadline IS NULL OR t.deadline <= :deadline)
                          AND (:isCompleted IS NULL OR t.isCompleted = :isCompleted)
                          AND (
                            (:parentTaskId IS NULL AND t.parentTaskId IS NULL)
                            OR (:parentTaskId IS NOT NULL AND t.parentTaskId = :parentTaskId)
                          )
                          AND (
                            :assigneesSize IS NULL OR :assigneesSize = 0 OR
                            (SELECT COUNT(DISTINCT a.name)
                             FROM t.assignees a
                             WHERE a.name IN :assignees
                            ) = :assigneesSize
                          )
                    """)
    Page<Task> getTasksFiltered(
            @Param("parentTaskId") UUID parentTaskId,
            @Param("priority") Priority priority,
            @Param("title") String title,
            @Param("deadline") LocalDate deadline,
            @Param("isCompleted") Boolean isCompleted,
            @Param("assignees") Set<String> assignees,
            @Param("assigneesSize") Long assigneesSize,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("delete from Task t where t.id = :id or t.parentTaskId = :id")
    int deleteTaskAndDirectChildren(@Param("id") UUID id);
}
