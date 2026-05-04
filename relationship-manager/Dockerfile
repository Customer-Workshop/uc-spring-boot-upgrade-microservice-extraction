FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
ARG SERVICE
COPY pom.xml .
COPY common/pom.xml common/
COPY ${SERVICE}/pom.xml ${SERVICE}/
COPY common/src common/src
COPY ${SERVICE}/src ${SERVICE}/src
RUN apk add --no-cache maven && \
    mvn -pl common -am clean install -DskipTests -q && \
    mvn -pl ${SERVICE} clean package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ARG SERVICE
COPY --from=build /app/${SERVICE}/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
