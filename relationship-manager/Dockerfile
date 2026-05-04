FROM gradle:8.5-jdk17 AS build
ARG SERVICE_NAME
WORKDIR /app
COPY ../common ./common
COPY ../${SERVICE_NAME} ./${SERVICE_NAME}
COPY ../build.gradle.kts ../settings.gradle.kts ./
RUN gradle :${SERVICE_NAME}:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
ARG SERVICE_NAME
WORKDIR /app
COPY --from=build /app/${SERVICE_NAME}/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
