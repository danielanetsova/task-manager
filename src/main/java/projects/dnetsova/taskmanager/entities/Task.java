package projects.dnetsova.taskmanager.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.Formula;
import projects.dnetsova.taskmanager.converter.PeriodStringConverter;
import projects.dnetsova.taskmanager.utils.Priority;

import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Basic
    private LocalDate deadline;

    /** Used only for ordering: 0 when deadline is set, 1 when null (sort ASC = nulls last). */
    @Formula("(CASE WHEN deadline IS NULL THEN 1 ELSE 0 END)")
    private Integer deadlineNullsLastSort;

    @Column(name = "repeat_date")
    private LocalDate repeatDate;

    @Convert(converter = PeriodStringConverter.class)
    @Column(name = "repeat_period")
    private Period repeatPeriod;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @ManyToMany
    @JoinTable(
            name = "tasks_users",
            joinColumns = @JoinColumn(
                    name = "task_id",
                    referencedColumnName = "id",
                    foreignKey = @ForeignKey(
                            name = "fk_tasks_users_task",
                            foreignKeyDefinition =
                                    "FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE"
                    )
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "user_id",
                    referencedColumnName = "id",
                    foreignKey = @ForeignKey(
                            name = "fk_tasks_users_user",
                            foreignKeyDefinition =
                                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                    )
            )
    )
    private Set<User> assignees;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "parent_task_id")
    private UUID parentTaskId;

    /** ID of the clone created from this task (populated for parent tasks only). */
    @Column(name = "clone_task_id")
    private UUID cloneTaskId;

    @Column(name = "is_completed")
    private boolean isCompleted;

    public Task(String title, String description, Priority priority, LocalDate startTime, LocalDate deadline,
                LocalDate repeat, Period repeatPeriod, Set<User> assignees, UUID parentTaskId, boolean isCompleted) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.startDate = startTime;
        this.deadline = deadline;
        this.repeatDate = repeat;
        this.repeatPeriod = repeatPeriod;
        this.assignees = assignees;
        this.parentTaskId = parentTaskId;
        this.isCompleted = isCompleted;
    }

    public Task() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public LocalDate getRepeatDate() {
        return repeatDate;
    }

    public void setRepeatDate(LocalDate repeatDate) {
        this.repeatDate = repeatDate;
    }

    public Period getRepeatPeriod() {
        return repeatPeriod;
    }

    public void setRepeatPeriod(Period repeatPeriod) {
        this.repeatPeriod = repeatPeriod;
    }

    public LocalDate getCompletionDate() { return completionDate; }

    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public Set<User> getAssignees() {
        return assignees;
    }

    public void setAssignees(Set<User> assignees) {
        this.assignees = assignees;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public UUID getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(UUID parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public UUID getCloneTaskId() {
        return cloneTaskId;
    }

    public void setCloneTaskId(UUID cloneTaskId) {
        this.cloneTaskId = cloneTaskId;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
