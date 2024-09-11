package projects.dnetsova.taskmanager.models;

public record ApiError(String name, String description) {
    public ApiError(Exception e) {
        // The "Exception" part is removed in order to make it consistent with the cases where we do not use exception to
        // create the ApiError
        this(e.getClass().getSimpleName().replace("Exception", ""), e.getMessage());
    }
}
