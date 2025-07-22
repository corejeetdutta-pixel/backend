# -------- Stage 1: Build the Spring Boot App --------
FROM eclipse-temurin:21-jdk-alpine as builder

# Set working directory inside builder container
WORKDIR /build

# Copy all source files
COPY . .

# Make mvnw executable
RUN chmod +x mvnw

# Build the app and skip tests
RUN ./mvnw clean package -DskipTests

# -------- Stage 2: Run the Spring Boot App --------
FROM eclipse-temurin:21-jdk-alpine

# Set working directory inside runtime container
WORKDIR /app

# Copy only the built jar from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
