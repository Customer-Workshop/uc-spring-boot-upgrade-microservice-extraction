FROM eclipse-temurin:11-jdk-jammy AS build
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle ./
RUN ./gradlew --no-daemon dependencies || true
COPY src/ src/
RUN ./gradlew --no-daemon clean build -x test -x spotlessCheck

FROM eclipse-temurin:11-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=default
ENTRYPOINT ["java", "-jar", "app.jar"]
