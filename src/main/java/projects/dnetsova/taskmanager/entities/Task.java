package projects.dnetsova.taskmanager.entities;

import jakarta.persistence.*;
import projects.dnetsova.taskmanager.utils.Priority;

import java.time.LocalDate;
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

    @Column(name = "repeat_date")
    private LocalDate repeatDate;

    @ManyToMany
    @JoinTable(name = "tasks_users",
            joinColumns = @JoinColumn(name = "task_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private Set<User> assignees;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Priority priority;
}
