package projects.dnetsova.taskmanager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "User created successfully."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
            description = "User name can not be empty/Username is taken."
            )
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createUser(@Parameter(description = "The name of the currently added user"
            , example = "Fred") @RequestParam String name)  {

        if (nameIsNotValid(name)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ApiResponse<>(null,
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

    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponse<Void>> removeUser(@RequestParam String name) {
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

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateUser(@RequestParam String name, @RequestParam String updatedName) {

        if (name.equals(updatedName)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                    new ApiError("InvalidName", "Current user name same as new user name.")));
        }

        if (nameIsNotValid(name) || nameIsNotValid(updatedName)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(null,
                    new ApiError("InvalidName", "User name cannot be empty.")));
        }

        try {
            userService.updateUser(name, updatedName);
        } catch (InvalidUserException | DuplicateUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, new ApiError(e)));
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/get-all")
    public ResponseEntity<ApiResponse<CustomPage<String>>> getAllUsers(
            @RequestParam(required = false, defaultValue = "1") int page,
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

    private boolean nameIsNotValid(String name) {
        return name == null || name.isBlank();
    }
}
