# syntax=docker/dockerfile:1

# Build stage. The dependency layer is cached separately from the source layer, so a
# source-only change does not re-download the world.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# Runtime stage: JRE only, and a non-root user.
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# curl is here for the container healthcheck, which has to work in a shell that is
# dash rather than bash, so /dev/tcp tricks are not available.
RUN apt-get update \
 && apt-get install --no-install-recommends -y curl \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system --gid 1001 app \
 && useradd --system --uid 1001 --gid app app

COPY --from=build /build/target/auth-service-1.0.0.jar app.jar
RUN chown -R app:app /app
USER app

EXPOSE 8080

# Container-aware heap sizing rather than a fixed -Xmx, so the same image behaves
# sensibly under a compose default and a Kubernetes memory limit.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
