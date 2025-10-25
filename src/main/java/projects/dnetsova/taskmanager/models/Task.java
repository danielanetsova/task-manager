package projects.dnetsova.taskmanager.models;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record Task(UUID id, String title, String description, String priority, LocalDate start,
                   LocalDate deadline, LocalDate repeat, List<String> assignees, UUID parentTaskId,
                   boolean isCompleted) {
}
