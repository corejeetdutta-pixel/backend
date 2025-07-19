# Stage 1: Build the app
FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /build

COPY . .

# Fix: make mvnw executable
RUN chmod +x ./mvnw

# Build the project
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the app
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the jar from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose port (Spring Boot default)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
