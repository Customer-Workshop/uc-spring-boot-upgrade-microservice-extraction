# Stage 1: Build
FROM gradle:7.4-jdk11 AS build
WORKDIR /app
COPY settings.gradle build.gradle ./
COPY shared-lib ./shared-lib
COPY api-gateway ./api-gateway
RUN gradle :api-gateway:bootJar --no-daemon -x test

# Stage 2: Runtime
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/api-gateway/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
