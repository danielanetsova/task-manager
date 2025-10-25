package projects.dnetsova.taskmanager.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import projects.dnetsova.taskmanager.models.CustomPage;
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
                Priority.valueOf(task.priority()),
                task.start(),
                task.deadline(),
                task.repeat(),
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
                entity.getPriority().toString(),
                entity.getStartDate(),
                entity.getDeadline(),
                entity.getRepeatDate(),
                entity.getAssignees().stream().map(a -> a.getName()).toList(),
                entity.getParentTaskId(),
                entity.isCompleted()
        );
    }

    public CustomPage<Task> getTasks(Priority priority, String title, LocalDate deadline, Set<String> assignees,
                                     int page, int size) {
        if (page <= 0) throw new IllegalArgumentException("Page must be greater than 0");
        if (size <= 0) throw new IllegalArgumentException("Size must be greater than 0");

        Page<projects.dnetsova.taskmanager.entities.Task> taskEntities =
                this.taskRepository.getTasksFiltered(
                        priority,
                        title,
                        deadline,
                        assignees,
                        (long) assignees.size(),
                        PageRequest.of(page - 1, size)
                );

        return new CustomPage<>(
                entitiesToModels(taskEntities.getContent()),
                taskEntities.getTotalPages(),
                taskEntities.getTotalElements()
        );
    }

    @Transactional
    public void updateTask(UUID id, TaskUpdate taskUpdate) {
        if (taskUpdate.getTitle() == null || taskUpdate.getPriority() == null || taskUpdate.getIsCompleted() == null) {
            throw new IllegalArgumentException("Title, priority and isCompleted cannot be set to null or empty");
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

        applyUpdate(taskUpdate.getIsCompleted(),
                entity::setCompleted);

        taskRepository.saveAndFlush(entity);
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
                te.getPriority().toString(),
                te.getStartDate(),
                te.getDeadline(),
                te.getRepeatDate(),
                te.getAssignees().stream().map(a -> a.getName()).toList(),
                te.getParentTaskId(),
                te.isCompleted()
        )).toList();
    }
}
