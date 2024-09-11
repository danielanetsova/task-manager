package projects.dnetsova.taskmanager.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Daniela Netsova",
                        email = "netsovadaniela@gmail.com"
                ),
                description = "OpenApi documentation for Task Manager",
                title = "Task Manager API specification",
                version = "0.1.0"
        ),
        servers = @Server(
                description = "Local environment",
                url = "http://localhost:8080"
)
)
public class OpenApiConfig {
}
