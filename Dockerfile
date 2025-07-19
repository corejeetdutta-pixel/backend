# Use a Java 21 base image
FROM eclipse-temurin:21-jdk-alpine
# Set working directory
WORKDIR /app
# Copy the JAR file from your local target folder to the container
COPY target/recruitmente2e-0.0.1-SNAPSHOT.jar app.jar
# Expose port (Spring Boot defaults to 8080)
EXPOSE 8080
# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]