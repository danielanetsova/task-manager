package projects.dnetsova.taskmanager.controllers;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import projects.dnetsova.taskmanager.models.ApiError;
import projects.dnetsova.taskmanager.models.ApiResponse;

import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {
    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public ResponseEntity<ApiResponse<?>> handleError(WebRequest webRequest) {
        // Get error details
        Map<String, Object> errors = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());

        HttpStatus status = HttpStatus.resolve((Integer) errors.get("status"));

        if (errors.get("error").equals("Not Found")) {
            return new ResponseEntity<>(new ApiResponse<>(null,
                    new ApiError("PathNotFound",
                            errors // errors,get(error).replace(" ", "")
                                .get("path").toString() + " path not found.")),
                    status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR); //500
        }

        return new ResponseEntity<>(new ApiResponse<>(null,
                new ApiError("GenericServerError",
                        errors
                        .get("error")
                        .toString()
                        .replace(" ", ""))), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
