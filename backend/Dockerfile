# Stage 1: Build the application
FROM maven:3.9.5-eclipse-temurin-17-focal AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-focal

# Set working directory
WORKDIR /app

# Add non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership and permissions
RUN chown appuser:appuser /app/app.jar
USER appuser

# Expose port
EXPOSE 8080

# Set health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Environment variables with defaults
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:+UseStringDeduplication -XX:MaxRAMPercentage=75"

# Start the application with proper memory settings and GC tuning
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]