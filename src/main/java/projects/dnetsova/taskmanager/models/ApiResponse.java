package projects.dnetsova.taskmanager.models;

public record ApiResponse<T> (T content, ApiError... errors) {
}
