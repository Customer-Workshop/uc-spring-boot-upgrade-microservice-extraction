FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src src
COPY article-service article-service
RUN chmod +x gradlew && ./gradlew bootJar -x test -x generateJava --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
