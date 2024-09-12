package projects.dnetsova.taskmanager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import openapi.OpenApiExamplesConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import projects.dnetsova.taskmanager.exceptions.DuplicateUserException;
import projects.dnetsova.taskmanager.exceptions.InvalidUserException;
import projects.dnetsova.taskmanager.models.ApiError;
import projects.dnetsova.taskmanager.models.ApiResponse;
import projects.dnetsova.taskmanager.models.CustomPage;
import projects.dnetsova.taskmanager.services.UserService;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create user", description = "A user with the given name is created and saved.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                    description = "User created successfully."
                    ,content = @Content(
                            examples = @ExampleObject(
                                name = "User created",
                                summary = "User created",
                                description = "Successful user creation response"
                            )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Username empty/Username taken.",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "User name empty",
                                            summary = "User name empty",
                                            description = "User name empty response",
                                            value = OpenApiExamplesConstants.INVALID_USER_NAME_ERROR
                                    ),
                                    @ExampleObject(
                                            name = "Username taken",
                                            summary = "Username taken",
                                            description = "Username taken response",
                                            value = OpenApiExamplesConstants.DUPLICATE_USER_ERROR
                                    )
                            }
                            )
            )
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createUser(@Parameter(description = "The name of the currently added user",
            example = "Fred") @RequestParam String name)  {

        if (nameIsNotValid(name)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(null,
                    new ApiError("InvalidName", "User name cannot be empty.")));
        }

        try {
            userService.addUser(name);
        } catch (DuplicateUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, new ApiError(e)));
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Remove user", description = "A user with the given name is removed.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "User removed successfully.",
                    content = @Content(examples = @ExampleObject(
                        name = "Response content",
                        summary = "Response content",
                        description = "Successful user deletion response")
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Username empty/User does not exist.",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "User name empty",
                                            summary = "User name empty",
                                            description = "User name empty response",
                                            value = OpenApiExamplesConstants.INVALID_USER_NAME_ERROR
                                    ),
                                    @ExampleObject(
                                            name = "User does not exist",
                                            summary = "User does not exist",
                                            description = "User does not exist response",
                                            value = OpenApiExamplesConstants.INVALID_USER_ERROR
                                    )
                            }
                    )
            )
    })
    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponse<Void>> removeUser(@Parameter(description = "The name of the user to be removed",
            example = "Fred") @RequestParam String name) {
        if (nameIsNotValid(name)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(null,
                    new ApiError("InvalidName", "User name cannot be empty.")));
        }

        try {
            userService.removeUser(name);
        } catch (InvalidUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, new ApiError(e)));
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Update user",
            description = "A user with the given name is updated with the new name.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
            description = "User renamed successfully.",
            content = @Content(examples =
                @ExampleObject(
                    name = "Response content",
                    summary = "Response content",
                    description = "Successful user update response"
            ))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
            description = "New user name taken/User name empty/User does not exist/Username taken.",
            content = @Content(examples = {
                    @ExampleObject(
                            name = "New user name taken",
                            summary = "New user name taken",
                            description = "New user name taken response",
                            value = OpenApiExamplesConstants.INVALID_USER_NAME_ERROR_2
                    ),
                    @ExampleObject(
                            name = "User name empty",
                            summary = "User name empty",
                            description = "User name empty response",
                            value = OpenApiExamplesConstants.INVALID_USER_NAME_ERROR
                    ),
                    @ExampleObject(
                            name = "User does not exist",
                            summary = "User does not exist",
                            description = "User does not exist response",
                            value = OpenApiExamplesConstants.INVALID_USER_ERROR
                    ),
                    @ExampleObject(
                            name = "Username taken",
                            summary = "Username taken",
                            description = "Username taken response",
                            value = OpenApiExamplesConstants.DUPLICATE_USER_ERROR_2
                    )
                  })
            )
        }
    )
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @Parameter(description = "The current name of the user to be updated",
            example = "Fred") @RequestParam String originalName,
            @Parameter(description = "The new name of the user to be updated",
            example = "Bob") @RequestParam String newName) {

        if (originalName.equals(newName)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                    new ApiError("InvalidName", "Current user name same as new user name.")));
        }

        if (nameIsNotValid(originalName) || nameIsNotValid(newName)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(null,
                    new ApiError("InvalidName", "User name cannot be empty.")));
        }

        try {
            userService.updateUser(originalName, newName);
        } catch (InvalidUserException | DuplicateUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, new ApiError(e)));
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Get a page of users",
               description = "Retrieves the desired page of users")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Page of users retrieved successfully.",
                    content = @Content(examples = @ExampleObject(
                            name = "Response content",
                            summary = "Response content",
                            description = "Successful page of users retrieval response",
                            value = OpenApiExamplesConstants.GET_ALL_USERS_SUCCESS)
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

    @GetMapping("/get")
    public ResponseEntity<ApiResponse<CustomPage<String>>> getUsersPage(
            @Parameter(description = "Page number", example = "1")
            @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(required = false, defaultValue = "10") int size) {

       try {
           CustomPage<String> allUsers = userService.getAllUsers(page, size);
           return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(allUsers));
       } catch (IllegalArgumentException e) {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(null, new ApiError(e)));
       }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    private ResponseEntity<ApiResponse<?>> handleMissingRequestParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                new ApiError("MissingRequestParameter", ex.getMessage())));
    }

    private boolean     nameIsNotValid(String name) {
        return name == null || name.isBlank();
    }
}
