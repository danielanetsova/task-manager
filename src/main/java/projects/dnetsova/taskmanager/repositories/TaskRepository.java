package projects.dnetsova.taskmanager.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projects.dnetsova.taskmanager.entities.Task;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
}
