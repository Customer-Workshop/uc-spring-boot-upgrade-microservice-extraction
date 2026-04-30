FROM openjdk:11-jdk-slim AS build

WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY src src

RUN chmod +x gradlew && ./gradlew clean bootJar -x test --no-daemon

FROM openjdk:11-jre-slim AS runtime

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/tags || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
