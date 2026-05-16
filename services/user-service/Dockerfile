FROM gradle:7.4-jdk11 AS build
WORKDIR /app
COPY settings.gradle ./settings.gradle
COPY build.gradle ./build.gradle
COPY services/common/ ./services/common/
COPY services/user-service/ ./services/user-service/
RUN gradle :services:user-service:bootJar --no-daemon -x test

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/services/user-service/build/libs/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
