package projects.dnetsova.taskmanager.specifications;

import org.springframework.data.jpa.domain.Specification;
import projects.dnetsova.taskmanager.entities.Task;
import projects.dnetsova.taskmanager.entities.User;

import jakarta.persistence.criteria.*;
import projects.dnetsova.taskmanager.utils.Priority;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public final class TaskSpecifications {

    private TaskSpecifications() {}

    public static Specification<Task> isCompleted(Boolean completed) {
        if (completed == null) return null;
        return (root, query, cb) -> cb.equal(root.get("isCompleted"), completed);
    }

    public static Specification<Task> priority(Priority priority) {
        if (priority == null) return null;
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> startDateLte(LocalDate date) {
        if (date == null) return null;
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("startDate"), date);
    }

    public static Specification<Task> startDate(LocalDate date) {
        if (date == null) {
            return (root, query, cb) ->
                    cb.or(
                            cb.lessThanOrEqualTo(root.get("startDate"), LocalDate.now()),
                            cb.isNull(root.get("startDate"))
                    );
        }
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.greaterThan(root.get("startDate"), date);
        };
    }

    public static Specification<Task> titleContainsIgnoreCase(String title) {
        if (title == null || title.isBlank()) return null;
        String pattern = "%" + title.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern);
    }

    public static Specification<Task> deadlineLte(LocalDate deadline) {
        if (deadline == null) return null;
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("deadline"), deadline);
    }

    public static Specification<Task> completionDate(LocalDate completionDate) {
        if (completionDate == null) return null;
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("completionDate"), completionDate);
    }

    public static Specification<Task> parentTaskIdEq(UUID parentTaskId) {
        if (parentTaskId == null) {
            return (root, query, cb) -> cb.isNull(root.get("parentTaskId"));
        }
        return (root, query, cb) ->
                cb.equal(root.get("parentTaskId"), parentTaskId);
    }

    /**
     * Requires the task to have ALL provided assignee names (like your old COUNT(DISTINCT ...) = size).
     * Uses a correlated subquery; performs well with proper join-table indexes.
     */
    public static Specification<Task> hasAllAssigneesByName(Set<String> assigneeNames) {
        if (assigneeNames == null || assigneeNames.isEmpty()) return null;

        return (root, query, cb) -> {
            // Avoid duplicate rows when other specs introduce joins
            query.distinct(true);

            Subquery<Long> sq = query.subquery(Long.class);
            Root<Task> sqTask = sq.from(Task.class);
            Join<Task, User> sqAssignees = sqTask.join("assignees", JoinType.INNER);

            sq.select(cb.countDistinct(sqAssignees.get("name")));
            sq.where(
                    cb.equal(sqTask.get("id"), root.get("id")),
                    sqAssignees.get("name").in(assigneeNames)
            );

            return cb.equal(sq, (long) assigneeNames.size());
        };
    }
}
