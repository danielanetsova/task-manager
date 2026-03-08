package projects.dnetsova.taskmanager.models;

import projects.dnetsova.taskmanager.utils.Priority;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record Task(UUID id, String title, String description, Priority priority, LocalDate start,
                   LocalDate deadline, LocalDate repeat, RepeatPeriod repeatPeriod, LocalDate completionDate,
                   List<String> assignees, UUID parentTaskId, boolean isCompleted) {
}
