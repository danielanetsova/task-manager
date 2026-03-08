package projects.dnetsova.taskmanager.jobs;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import projects.dnetsova.taskmanager.services.TaskEscalationService;

@Component
public class TaskEscalationJob {
    private final TaskEscalationService service;

    public TaskEscalationJob(TaskEscalationService service) {
        this.service = service;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        service.escalatePriorities();
    }

    // run daily
    @Scheduled(cron = "0 0 0 * * *") // midnight
    public void runDaily() {
        service.escalatePriorities();
    }
}