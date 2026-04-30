FROM openjdk:11-jdk-slim AS build

WORKDIR /app
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle ./
RUN chmod +x gradlew

COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test -x spotlessJavaCheck

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
