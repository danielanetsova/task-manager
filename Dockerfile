# Use an official Java runtime as a parent image
FROM eclipse-temurin:17-jdk-jammy

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/task-manager-0.1.0.jar app.jar

# Expose the port your app runs on (default for Spring Boot is 8080)
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java","-jar","app.jar"]
