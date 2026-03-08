package projects.dnetsova.taskmanager.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projects.dnetsova.taskmanager.entities.Task;
import projects.dnetsova.taskmanager.utils.Priority;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class TaskEscalationService {
    private static final Logger log = LoggerFactory.getLogger(TaskEscalationService.class);

    private final TaskService taskService;

    public TaskEscalationService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Transactional
    public void escalatePriorities() {
        log.info("Starting tasks escalation");

        LocalDate today = LocalDate.now();
        int page = 0;
        int size = 2000;
        int totalPages = -1;

        do {
            Page<Task> result = taskService.getActiveTasksWithDeadline(page, size);
            if (totalPages == -1) {
                totalPages = result.getTotalPages();
            }
            for (Task task : taskService.getActiveTasksWithDeadline(page, size).getContent()) {

                long totalDays = ChronoUnit.DAYS.between(
                        task.getStartDate(),
                        task.getDeadline()
                );

                long passedDays = ChronoUnit.DAYS.between(
                        task.getStartDate(),
                        today
                );

                if (passedDays < 0) {
                    passedDays = 0;
                }

                if (passedDays > totalDays) {
                    passedDays = totalDays;
                }

                long percentage = totalDays == 0 ? 100 : passedDays * 100 / totalDays;

                Priority newPriority = determinePriority(percentage);

                if (newPriority != null &&
                        newPriority.isHigherThan(task.getPriority())) {

                    task.setPriority(newPriority);
                }
            }
            page++;
        } while (page < totalPages);

        log.info("Finished tasks escalation");
    }

    private Priority determinePriority(long percentage) {

        if (percentage > 80) return Priority.P0;
        if (percentage >= 60) return Priority.P1;
        if (percentage >= 40) return Priority.P2;
        if (percentage >= 20) return Priority.P3;

        return null;
    }
}
