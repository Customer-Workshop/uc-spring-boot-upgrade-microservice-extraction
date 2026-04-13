package io.spring.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Installation tests that verify Docker build configuration, docker-compose setup, service
 * configuration, and port assignments are correct for the microservice extraction.
 *
 * <p>These tests validate the deployment artifacts without requiring Docker to be running.
 */
@DisplayName("Installation Tests")
public class InstallationTest {

  private static final String PROJECT_ROOT = findProjectRoot();

  private static String findProjectRoot() {
    Path current = Paths.get(System.getProperty("user.dir"));
    while (current != null) {
      if (Files.exists(current.resolve("docker-compose.yml"))) {
        return current.toString();
      }
      current = current.getParent();
    }
    return System.getProperty("user.dir");
  }

  @Nested
  @DisplayName("Dockerfile Validation")
  class DockerfileValidation {

    @Test
    @DisplayName("Monolith Dockerfile exists and has correct structure")
    void monolithDockerfileShouldExist() {
      File dockerfile = new File(PROJECT_ROOT, "Dockerfile");
      assertThat(dockerfile).exists();
    }

    @Test
    @DisplayName("Monolith Dockerfile exposes port 8080")
    void monolithDockerfileShouldExposePort8080() throws Exception {
      String content = readFile("Dockerfile");
      assertThat(content).contains("EXPOSE 8080");
    }

    @Test
    @DisplayName("Monolith Dockerfile uses Java 11")
    void monolithDockerfileShouldUseJava11() throws Exception {
      String content = readFile("Dockerfile");
      assertThat(content).contains("jdk11").contains("openjdk:11");
    }

    @Test
    @DisplayName("Monolith Dockerfile uses multi-stage build")
    void monolithDockerfileShouldUseMultiStageBuild() throws Exception {
      String content = readFile("Dockerfile");
      assertThat(content).contains("AS build");
      assertThat(content).contains("COPY --from=build");
    }

    @Test
    @DisplayName("Monolith Dockerfile does not reference nonexistent files")
    void monolithDockerfileShouldNotReferenceSettingsGradle() throws Exception {
      String content = readFile("Dockerfile");
      assertThat(content).doesNotContain("settings.gradle");
    }

    @Test
    @DisplayName("Comments service Dockerfile exists and has correct structure")
    void commentsServiceDockerfileShouldExist() {
      File dockerfile = new File(PROJECT_ROOT, "comments-service/Dockerfile");
      assertThat(dockerfile).exists();
    }

    @Test
    @DisplayName("Comments service Dockerfile exposes port 8081")
    void commentsServiceDockerfileShouldExposePort8081() throws Exception {
      String content = readFile("comments-service/Dockerfile");
      assertThat(content).contains("EXPOSE 8081");
    }

    @Test
    @DisplayName("Comments service Dockerfile uses Java 11")
    void commentsServiceDockerfileShouldUseJava11() throws Exception {
      String content = readFile("comments-service/Dockerfile");
      assertThat(content).contains("jdk11").contains("openjdk:11");
    }
  }

  @Nested
  @DisplayName("Docker Compose Validation")
  class DockerComposeValidation {

    @Test
    @DisplayName("docker-compose.yml exists")
    void dockerComposeFileShouldExist() {
      File dockerCompose = new File(PROJECT_ROOT, "docker-compose.yml");
      assertThat(dockerCompose).exists();
    }

    @Test
    @DisplayName("docker-compose.yml defines both services")
    void dockerComposeShouldDefineBothServices() throws Exception {
      String content = readFile("docker-compose.yml");
      assertThat(content).contains("comments-service:");
      assertThat(content).contains("monolith:");
    }

    @Test
    @DisplayName("docker-compose.yml maps correct ports")
    void dockerComposeShouldMapCorrectPorts() throws Exception {
      String content = readFile("docker-compose.yml");
      assertThat(content).contains("8080:8080");
      assertThat(content).contains("8081:8081");
    }

    @Test
    @DisplayName("docker-compose.yml configures comments service URL for monolith")
    void dockerComposeShouldConfigureCommentsServiceUrl() throws Exception {
      String content = readFile("docker-compose.yml");
      assertThat(content).contains("comments-service:8081");
    }

    @Test
    @DisplayName("docker-compose.yml has healthcheck for comments-service")
    void dockerComposeShouldHaveHealthcheck() throws Exception {
      String content = readFile("docker-compose.yml");
      assertThat(content).contains("healthcheck:");
    }

    @Test
    @DisplayName("Monolith depends on comments-service being healthy")
    void monolithShouldDependOnCommentsService() throws Exception {
      String content = readFile("docker-compose.yml");
      assertThat(content).contains("depends_on:");
      assertThat(content).contains("service_healthy");
    }
  }

  @Nested
  @DisplayName("Application Configuration Validation")
  class ApplicationConfigValidation {

    @Test
    @DisplayName("Monolith application.properties contains comments service URL")
    void monolithShouldHaveCommentsServiceUrl() throws Exception {
      String content = readFile("src/main/resources/application.properties");
      assertThat(content).contains("comments.service.url");
    }

    @Test
    @DisplayName("Monolith test properties contain comments service URL")
    void monolithTestPropertiesShouldHaveCommentsServiceUrl() throws Exception {
      String content = readFile("src/main/resources/application-test.properties");
      assertThat(content).contains("comments.service.url");
    }

    @Test
    @DisplayName("Comments service application.properties configures port 8081")
    void commentsServiceShouldConfigurePort8081() throws Exception {
      String content = readFile("comments-service/src/main/resources/application.properties");
      assertThat(content).contains("server.port=8081");
    }

    @Test
    @DisplayName("Comments service uses H2 in-memory database")
    void commentsServiceShouldUseH2Database() throws Exception {
      String content = readFile("comments-service/src/main/resources/application.properties");
      assertThat(content).contains("h2:mem");
    }

    @Test
    @DisplayName("Comments service has JPA auto-DDL configured")
    void commentsServiceShouldHaveJpaConfig() throws Exception {
      String content = readFile("comments-service/src/main/resources/application.properties");
      assertThat(content).contains("hibernate.ddl-auto");
    }
  }

  @Nested
  @DisplayName("Project Structure Validation")
  class ProjectStructureValidation {

    @Test
    @DisplayName("Comments service has its own build.gradle")
    void commentsServiceShouldHaveBuildGradle() {
      File buildGradle = new File(PROJECT_ROOT, "comments-service/build.gradle");
      assertThat(buildGradle).exists();
    }

    @Test
    @DisplayName("Comments service has Spring Boot application class")
    void commentsServiceShouldHaveApplicationClass() {
      File appClass =
          new File(
              PROJECT_ROOT,
              "comments-service/src/main/java/io/spring/comments/CommentsServiceApplication.java");
      assertThat(appClass).exists();
    }

    @Test
    @DisplayName("Comments service has REST controller")
    void commentsServiceShouldHaveController() {
      File controller =
          new File(
              PROJECT_ROOT,
              "comments-service/src/main/java/io/spring/comments/controller/CommentController.java");
      assertThat(controller).exists();
    }

    @Test
    @DisplayName("Comments service has JPA entity")
    void commentsServiceShouldHaveEntity() {
      File entity =
          new File(
              PROJECT_ROOT,
              "comments-service/src/main/java/io/spring/comments/model/CommentEntity.java");
      assertThat(entity).exists();
    }

    @Test
    @DisplayName("Comments service has repository")
    void commentsServiceShouldHaveRepository() {
      File repo =
          new File(
              PROJECT_ROOT,
              "comments-service/src/main/java/io/spring/comments/repository/CommentRepository.java");
      assertThat(repo).exists();
    }

    @Test
    @DisplayName("Comments service has service layer")
    void commentsServiceShouldHaveServiceLayer() {
      File service =
          new File(
              PROJECT_ROOT,
              "comments-service/src/main/java/io/spring/comments/service/CommentService.java");
      assertThat(service).exists();
    }

    @Test
    @DisplayName("Comments service has its own test class")
    void commentsServiceShouldHaveTests() {
      File test =
          new File(
              PROJECT_ROOT,
              "comments-service/src/test/java/io/spring/comments/CommentsServiceApplicationTest.java");
      assertThat(test).exists();
    }

    @Test
    @DisplayName("Monolith has CommentServiceClient for HTTP communication")
    void monolithShouldHaveCommentServiceClient() {
      File client =
          new File(
              PROJECT_ROOT,
              "src/main/java/io/spring/infrastructure/service/CommentServiceClient.java");
      assertThat(client).exists();
    }

    @Test
    @DisplayName("Monolith has RestTemplateConfig")
    void monolithShouldHaveRestTemplateConfig() {
      File config = new File(PROJECT_ROOT, "src/main/java/io/spring/RestTemplateConfig.java");
      assertThat(config).exists();
    }
  }

  private String readFile(String relativePath) throws Exception {
    return new String(Files.readAllBytes(Paths.get(PROJECT_ROOT, relativePath)));
  }
}
