# Stage 1: Build
FROM gradle:7.4-jdk11 AS build
WORKDIR /app
COPY settings.gradle build.gradle ./
COPY shared-lib ./shared-lib
COPY user-service ./user-service
RUN gradle :user-service:bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /app/user-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
