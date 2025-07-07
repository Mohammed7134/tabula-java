# Use a minimal Java runtime image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the JAR file (adjust the name if different)
COPY target/tabulaweb-1.0.6-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
