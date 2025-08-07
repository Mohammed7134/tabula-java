# Stage 1: Build the JAR using Maven
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# Package the application (skip tests if desired)
RUN mvn clean package -DskipTests

# Stage 2: Use the JAR in a lightweight runtime image
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the built JAR from the first stage
COPY --from=build /app/target/tabulaweb-1.0.6-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
