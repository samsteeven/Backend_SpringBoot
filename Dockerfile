# Multi-stage Dockerfile for Spring Boot Backend
# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Créer les dossiers utilisés par logback et l'app
RUN mkdir -p /app/logs /app/uploads \
    && chown -R spring:spring /app
    
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/target/easypharma_backend-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
