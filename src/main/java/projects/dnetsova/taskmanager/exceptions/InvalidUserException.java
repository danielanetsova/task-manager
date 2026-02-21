package projects.dnetsova.taskmanager.exceptions;

import java.util.UUID;

public class InvalidUserException extends Exception {
    public InvalidUserException(String user) {
        super("User '" + user + "' does not exist");
    }

    public InvalidUserException(UUID id) {
        super("User with ID '" + id + "' does not exist");
    }
}
