FROM gradle:7.4-jdk17 AS build
WORKDIR /app
COPY build.gradle ./
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x spotlessJava -x spotlessCheck -x test -x jacocoTestReport -x jacocoTestCoverageVerification

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
