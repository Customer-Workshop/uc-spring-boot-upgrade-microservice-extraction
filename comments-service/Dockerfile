# syntax=docker/dockerfile:1

# --- Build stage ---
FROM eclipse-temurin:11-jdk AS build
WORKDIR /workspace

COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY settings.gradle build.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# --- Runtime stage ---
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
