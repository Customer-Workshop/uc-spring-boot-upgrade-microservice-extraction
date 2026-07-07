# Build stage
FROM gradle:7.4-jdk11 AS build
WORKDIR /workspace
COPY build.gradle ./
COPY src ./src
RUN gradle bootJar --no-daemon -x test

# Run stage
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENV COMMENTS_SERVICE_URL=http://comments-service:8081
ENTRYPOINT ["java", "-jar", "app.jar"]
