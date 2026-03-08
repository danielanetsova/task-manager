package projects.dnetsova.taskmanager.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import projects.dnetsova.taskmanager.models.CustomPage;
import projects.dnetsova.taskmanager.models.RepeatPeriod;
import projects.dnetsova.taskmanager.models.Task;
import projects.dnetsova.taskmanager.models.TaskUpdate;
import projects.dnetsova.taskmanager.repositories.TaskRepository;
import projects.dnetsova.taskmanager.repositories.UserRepository;
import projects.dnetsova.taskmanager.utils.Priority;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static projects.dnetsova.taskmanager.specifications.TaskSpecifications.completionDate;
import static projects.dnetsova.taskmanager.specifications.TaskSpecifications.deadlineIsNotNull;
import static projects.dnetsova.taskmanager.specifications.TaskSpecifications.deadlineLte;
import static projects.dnetsova.taskmanager.specifications.TaskSpecifications.hasAllAssigneesByName;
import static projects.dnetsova.taskmanager.specifications.TaskSpecifications.isCompleted;
import static projects.dnetsova.taskmanager.specifications.TaskSpecifications.parentTaskIdEq;
import static projects.dnetsova.taskmanager.specifications.TaskSpecifications.priority;
import static projects.dnetsova.taskmanager.specifications.TaskSpecifications.startDate;
import static projects.dnetsova.taskmanager.specifications.TaskSpecifications.startDateLte;
import static projects.dnetsova.taskmanager.specifications.TaskSpecifications.titleContainsIgnoreCase;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public void addTask(Task task) {
        projects.dnetsova.taskmanager.entities.Task taskEntity = new projects.dnetsova.taskmanager.entities.Task(
                task.title(),
                task.description(),
                task.priority(),
                task.start() == null ? LocalDate.now() : task.start(),
                task.deadline(),
                task.repeat(),
                task.repeatPeriod() == null ? null : task.repeatPeriod().toPeriod(),
                new HashSet<>(this.userRepository.findByNameIn(task.assignees())),
                task.parentTaskId(),
                task.isCompleted()
        );
        this.taskRepository.saveAndFlush(taskEntity);
    }

    public Task getTaskById(UUID id) {
        projects.dnetsova.taskmanager.entities.Task entity = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + id));

        return new Task(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPriority(),
                entity.getStartDate(),
                entity.getDeadline(),
                entity.getRepeatDate(),
                entity.getRepeatPeriod() != null ? RepeatPeriod.fromPeriod(entity.getRepeatPeriod()) : null,
                entity.getCompletionDate(),
                entity.getAssignees().stream().map(a -> a.getName()).toList(),
                entity.getParentTaskId(),
                entity.isCompleted()
        );
    }

    public CustomPage<Task> getTasks(
            UUID parentTaskId,
            Priority priority,
            String title,
            LocalDate startDate,
            LocalDate deadline,
            LocalDate completionDate,
            Boolean isCompleted,
            Set<String> assignees,
            int page,
            int size
    ) {
        Specification<projects.dnetsova.taskmanager.entities.Task> spec = Specification
                .<projects.dnetsova.taskmanager.entities.Task>where(null)
                .and(isCompleted(isCompleted))
                .and(priority(priority))
                .and(titleContainsIgnoreCase(title))
                .and(
                        (isCompleted != null && isCompleted) || (parentTaskId != null && startDate == null) ?
                                null : startDate(startDate))
                .and(deadlineLte(deadline))
                .and(completionDate(completionDate))
                .and(parentTaskIdEq(parentTaskId))
                .and(hasAllAssigneesByName(assignees));

        Sort sort;
        if (startDate != null) {
            sort = Sort.by(Sort.Order.asc("startDate").nullsLast());
        } else if (isCompleted != null && !isCompleted) {
            // Sort by priority, then by formula (0=has deadline, 1=null) so nulls last, then by deadline
            sort = Sort.by(
                    Sort.Order.asc("priority"),
                    Sort.Order.asc("deadlineNullsLastSort"),
                    Sort.Order.asc("deadline")
            );
        } else {
            sort = Sort.by(Sort.Order.asc("completionDate"));
        }

        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<projects.dnetsova.taskmanager.entities.Task> result = taskRepository.findAll(spec, pageable);

        return new CustomPage<>(
                entitiesToModels(result.getContent()),
                result.getTotalPages(),
                result.getTotalElements()
        );
    }

    @Transactional
    public void deleteTask(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new NoSuchElementException("Task not found: " + id);
        }

        taskRepository.deleteTaskAndDirectChildren(id);
    }

    @Transactional
    public void updateTask(UUID id, TaskUpdate taskUpdate) {
        if (taskUpdate.getTitle() == null || taskUpdate.getPriority() == null) {
            throw new IllegalArgumentException("Title and priority cannot be set to null or empty");
        }

        projects.dnetsova.taskmanager.entities.Task entity = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + id));

        applyUpdate(taskUpdate.getTitle(),
                entity::setTitle);

        applyUpdate(taskUpdate.getDescription(),
                entity::setDescription);

        applyUpdate(
                taskUpdate.getPriority().isPresent() ?
                        Optional.of(Priority.valueOf(taskUpdate.getPriority().get())) : Optional.empty(),
                entity::setPriority);

        applyUpdate(taskUpdate.getStart(),
                entity::setStartDate);

        applyUpdate(taskUpdate.getDeadline(),
                entity::setDeadline);

        applyUpdate(taskUpdate.getRepeat(),
                entity::setRepeatDate);

        if (taskUpdate.getAssignees() == null) {
            entity.setAssignees(Collections.emptySet());
        } else if (taskUpdate.getAssignees().isPresent()) {
            entity.setAssignees(new HashSet<>(userRepository.findByNameIn(taskUpdate.getAssignees().get())));
        }

        taskRepository.saveAndFlush(entity);
    }

    @Transactional
    public void completeTask(UUID id, boolean disableRepeat) {
        projects.dnetsova.taskmanager.entities.Task entity = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + id));

        if (!entity.isCompleted()) {
            LocalDate completionDate = LocalDate.now();
            entity.setCompleted(true);
            entity.setCompletionDate(completionDate);
            taskRepository.saveAndFlush(entity);

            if (!disableRepeat && entity.getParentTaskId() == null) {
                // Create a new recurring task if repeat information is present
                LocalDate newStartDate = null;
                if (entity.getRepeatDate() != null) {
                    newStartDate = entity.getRepeatDate();
                } else if (entity.getRepeatPeriod() != null) {
                    newStartDate = completionDate.plus(entity.getRepeatPeriod());
                }

                if (newStartDate != null) {
                    LocalDate newDeadline = null;
                    if (entity.getDeadline() != null) {
                        // Preserve the same offset between start and deadline
                        java.time.Period offset = java.time.Period.between(entity.getStartDate(), entity.getDeadline());
                        newDeadline = newStartDate.plus(offset);
                    }

                    // If repeatDate was set, new task's repeatDate is same date next year (Feb 29 -> Feb 28 in non-leap years)
                    LocalDate newRepeatDate = entity.getRepeatDate() != null
                            ? entity.getRepeatDate().plusYears(1)
                            : null;

                    projects.dnetsova.taskmanager.entities.Task newTask =
                            new projects.dnetsova.taskmanager.entities.Task(
                                    entity.getTitle(),
                                    entity.getDescription(),
                                    entity.getPriority(),
                                    newStartDate,
                                    newDeadline,
                                    newRepeatDate,
                                    entity.getRepeatPeriod(),
                                    // Use a new Set instance to avoid sharing the same collection reference
                                    new java.util.HashSet<>(entity.getAssignees()),
                                    entity.getParentTaskId(),
                                    false
                            );

                    taskRepository.saveAndFlush(newTask);

                    // Set cloneTaskId on the original parent task to reference the new clone
                    entity.setCloneTaskId(newTask.getId());
                    taskRepository.saveAndFlush(entity);

                    // Clone all subtasks of the completed task as subtasks of the new task
                    List<projects.dnetsova.taskmanager.entities.Task> children =
                            taskRepository.findByParentTaskId(entity.getId());
                    for (projects.dnetsova.taskmanager.entities.Task child : children) {
                        LocalDate subDeadline = null;
                        if (child.getDeadline() != null) {
                            java.time.Period offset =
                                    java.time.Period.between(child.getStartDate(), child.getDeadline());
                            subDeadline = newStartDate.plus(offset);
                        }
                        projects.dnetsova.taskmanager.entities.Task clonedSubtask =
                                new projects.dnetsova.taskmanager.entities.Task(
                                        child.getTitle(),
                                        child.getDescription(),
                                        child.getPriority(),
                                        newStartDate,
                                        subDeadline,
                                        child.getRepeatDate(),
                                        child.getRepeatPeriod(),
                                        new HashSet<>(child.getAssignees()),
                                        newTask.getId(),
                                        false
                                );
                        taskRepository.saveAndFlush(clonedSubtask);
                    }
                }
            }
        }
    }

    @Transactional
    public void revertTask(UUID id) {
        projects.dnetsova.taskmanager.entities.Task entity = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + id));

        entity.setCompleted(false);
        entity.setCompletionDate(null);

        UUID cloneId = entity.getCloneTaskId();
        if (cloneId != null) {
            taskRepository.deleteTaskAndDirectChildren(cloneId);
        }
        entity.setCloneTaskId(null);

        taskRepository.saveAndFlush(entity);
    }

    /**
     * Retrieves all tasks with deadline != null, startDate &gt;= today, and isCompleted = false.
     */
    public Page<projects.dnetsova.taskmanager.entities.Task> getActiveTasksWithDeadline(int page, int size) {
        Specification<projects.dnetsova.taskmanager.entities.Task> spec = Specification
                .<projects.dnetsova.taskmanager.entities.Task>where(null)
                .and(isCompleted(false))
                .and(deadlineIsNotNull())
                .and(startDateLte(LocalDate.now()));

        return taskRepository.findAll(spec, PageRequest.of(page, size));
    }

    private <T> void applyUpdate(Optional<T> updateValue,
                                 Consumer<T> setter) {
        if (updateValue == null) {
            setter.accept(null);
        } else if (updateValue.isPresent()) {
            setter.accept(updateValue.get());
        }
    }

    private List<Task> entitiesToModels(List<projects.dnetsova.taskmanager.entities.Task> taskEntities) {
        return taskEntities.stream().map(te -> new Task(
                te.getId(),
                te.getTitle(),
                te.getDescription(),
                te.getPriority(),
                te.getStartDate(),
                te.getDeadline(),
                te.getRepeatDate(),
                te.getRepeatPeriod() != null ? RepeatPeriod.fromPeriod(te.getRepeatPeriod()) : null,
                te.getCompletionDate(),
                te.getAssignees().stream().map(a -> a.getName()).toList(),
                te.getParentTaskId(),
                te.isCompleted()
        )).toList();
    }
}
