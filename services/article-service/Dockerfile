# Stage 1: Build
FROM gradle:7.4-jdk11 AS build
WORKDIR /app
COPY settings.gradle build.gradle ./
COPY shared-lib ./shared-lib
COPY article-service ./article-service
RUN gradle :article-service:bootJar --no-daemon -x test

# Stage 2: Runtime
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/article-service/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
