package projects.dnetsova.taskmanager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import openapi.OpenApiExamplesConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import projects.dnetsova.taskmanager.models.ApiError;
import projects.dnetsova.taskmanager.models.ApiResponse;
import projects.dnetsova.taskmanager.models.CustomPage;
import projects.dnetsova.taskmanager.models.Task;
import projects.dnetsova.taskmanager.models.TaskUpdate;
import projects.dnetsova.taskmanager.services.TaskService;
import projects.dnetsova.taskmanager.utils.Priority;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/tasks")
public class TasksController {
    private final TaskService taskService;

    @Autowired
    public TasksController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Get a page of tasks",
            description = "Retrieves the desired page of tasks")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Page of tasks retrieved successfully.",
                    content = @Content(examples = @ExampleObject(
                            name = "Response content",
                            summary = "Response content",
                            description = "Successful page of tasks retrieval response",
                            value = OpenApiExamplesConstants.GET_ALL_TASKS_SUCCESS)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Invalid Page/Size",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid page",
                                            summary = "Invalid page",
                                            description = "Invalid page response",
                                            value = OpenApiExamplesConstants.PAGE_LESS_THAN_1
                                    ),
                                    @ExampleObject(
                                            name = "Invalid size",
                                            summary = "Invalid size",
                                            description = "Invalid size response",
                                            value = OpenApiExamplesConstants.SIZE_LESS_THAN_1
                                    )
                            }
                    )
            )
    })
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<CustomPage<Task>>> getTasksPage(
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String title,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate completionDate,
            @RequestParam(required = false) Boolean isCompleted,
            @RequestParam(required = false) Set<String> assignees,
            @RequestParam(required = false) UUID parentTaskId,
            @Parameter(description = "Page number", example = "1")
            @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(required = false, defaultValue = "10") int size) {
        try {
            CustomPage<Task> tasks = taskService.getTasks(
                    parentTaskId,
                    priority,
                    title,
                    startDate,
                    deadline,
                    completionDate,
                    isCompleted,
                    assignees == null ? Collections.emptySet(): assignees,
                    page,
                    size
            );
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(tasks));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(null, new ApiError(e)));
        }
    }

    @Operation(summary = "Add task", description = "A task with the given details is created and saved.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                    description = "Task created successfully."
                    ,content = @Content(
                    examples = @ExampleObject(
                            name = "Task created",
                            summary = "Task created",
                            description = "Successful task creation response"
                    )
            )
            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "400", description = "Username empty/Username taken.",
//                    content = @Content(
//                            examples = {
//                                    @ExampleObject(
//                                            name = "User name empty",
//                                            summary = "User name empty",
//                                            description = "User name empty response",
//                                            value = OpenApiExamplesConstants.INVALID_USER_NAME_ERROR
//                                    ),
//                                    @ExampleObject(
//                                            name = "Username taken",
//                                            summary = "Username taken",
//                                            description = "Username taken response",
//                                            value = OpenApiExamplesConstants.DUPLICATE_USER_ERROR
//                                    )
//                            }
//                    )
//            )
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createTask(
            @Parameter(description = "The task details of the currently added task") @RequestBody Task task)  {
        try {
            taskService.addTask(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(null, new ApiError(e)));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable UUID id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(task));
    }

    //TODO: More error handling
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateTask(
            @PathVariable UUID id,
            @RequestBody TaskUpdate update
    ) {
        try {
            taskService.updateTask(id, update);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(null, new ApiError(e)));
        }
    }

    @Operation(summary = "Complete task",
            description = "Marks the task with the given ID as completed. If the task is already completed, no change is made. " +
                    "When disableRepeat is true, no clone task is created even if the task has repeatDate or repeatPeriod.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Task completed or was already completed."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Task not found.")
    })
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> completeTask(
            @Parameter(description = "The ID of the task to mark as complete") @PathVariable UUID id,
            @Parameter(description = "If true, do not create a clone task even when the task has repeatDate or repeatPeriod")
            @RequestParam(required = false, defaultValue = "false") Boolean disableRepeat
    ) {
        try {
            taskService.completeTask(id, disableRepeat);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(null, new ApiError(e)));
        }
    }

    @Operation(summary = "Revert task",
            description = "Marks the task as not completed. If the task has a clone (cloneTaskId), the clone and all its child tasks are deleted.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Task reverted successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Task not found.")
    })
    @PatchMapping("/{id}/revert")
    public ResponseEntity<ApiResponse<Void>> revertTask(
            @Parameter(description = "The ID of the task to revert") @PathVariable UUID id
    ) {
        try {
            taskService.revertTask(id);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(null, new ApiError(e)));
        }
    }

    @Operation(summary = "Delete task", description = "A task with the given ID is deleted.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Task deleted successfully.",
                    content = @Content(examples = @ExampleObject(
                            name = "Response content",
                            summary = "Response content",
                            description = "Successful task deletion response")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Task not found.",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Task not found",
                                    summary = "Task not found",
                                    description = "Task not found response"
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @Parameter(description = "The ID of the task to be deleted") @PathVariable UUID id
    ) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(null, new ApiError(e)));
        }
    }
}
