package projects.dnetsova.taskmanager.exceptions;

public class InvalidUserException extends Exception {
    public InvalidUserException(String user) {
        super("User '" + user + "' does not exist");
    }
}
