FROM openjdk:11-jdk-slim AS builder
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle settings.gradle* ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test -x spotlessJavaCheck

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
