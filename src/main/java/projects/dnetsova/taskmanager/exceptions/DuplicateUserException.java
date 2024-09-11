package projects.dnetsova.taskmanager.exceptions;

public class DuplicateUserException extends Exception{
    public DuplicateUserException(String name) {
        super("User '" + name + "' is taken.");
    }
}
