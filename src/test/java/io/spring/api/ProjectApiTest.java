package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.data.ProjectData;
import io.spring.application.project.ProjectCommandService;
import io.spring.application.project.ProjectQueryService;
import io.spring.core.project.Project;
import io.spring.core.project.ProjectRepository;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ProjectApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ProjectApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ProjectQueryService projectQueryService;

  @MockBean private ProjectRepository projectRepository;

  @MockBean private ProjectCommandService projectCommandService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_read_project_success() throws Exception {
    DateTime now = new DateTime();
    Project project = new Project("Test Project", "Description", "Client", now, "active",
        user.getId());
    ProjectData projectData =
        new ProjectData(
            project.getId(),
            "Test Project",
            "Description",
            "Client",
            now,
            "active",
            user.getId(),
            now,
            now);

    when(projectQueryService.findById(eq(project.getId())))
        .thenReturn(Optional.of(projectData));

    RestAssuredMockMvc.when()
        .get("/projects/{id}", project.getId())
        .then()
        .statusCode(200)
        .body("project.name", equalTo("Test Project"))
        .body("project.status", equalTo("active"));
  }

  @Test
  public void should_404_if_project_not_found() throws Exception {
    when(projectQueryService.findById(anyString())).thenReturn(Optional.empty());
    RestAssuredMockMvc.when().get("/projects/not-exists").then().statusCode(404);
  }

  @Test
  public void should_update_project_success() throws Exception {
    DateTime now = new DateTime();
    Project originalProject =
        new Project("Old Name", "Old Description", "Old Client", now, "active", user.getId());

    Project updatedProject =
        new Project("New Name", "New Description", "New Client", now, "completed", user.getId());

    Map<String, Object> updateParam =
        prepareUpdateParam("New Name", "New Description", "New Client", "completed");

    ProjectData updatedProjectData =
        new ProjectData(
            updatedProject.getId(),
            "New Name",
            "New Description",
            "New Client",
            now,
            "completed",
            user.getId(),
            now,
            now);

    when(projectRepository.findById(eq(originalProject.getId())))
        .thenReturn(Optional.of(originalProject));
    when(projectCommandService.updateProject(eq(originalProject), any()))
        .thenReturn(updatedProject);
    when(projectQueryService.findById(eq(updatedProject.getId())))
        .thenReturn(Optional.of(updatedProjectData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(updateParam)
        .when()
        .put("/projects/{id}", originalProject.getId())
        .then()
        .statusCode(200)
        .body("project.name", equalTo("New Name"))
        .body("project.status", equalTo("completed"));
  }

  @Test
  public void should_get_403_if_not_author_to_update_project() throws Exception {
    DateTime now = new DateTime();
    User anotherUser = new User("test@test.com", "test", "123123", "", "");

    Project project =
        new Project("Project", "Description", "Client", now, "active", anotherUser.getId());

    Map<String, Object> updateParam =
        prepareUpdateParam("New Name", "New Description", "New Client", "completed");

    when(projectRepository.findById(eq(project.getId()))).thenReturn(Optional.of(project));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(updateParam)
        .when()
        .put("/projects/{id}", project.getId())
        .then()
        .statusCode(403);
  }

  @Test
  public void should_delete_project_success() throws Exception {
    DateTime now = new DateTime();
    Project project =
        new Project("Project", "Description", "Client", now, "active", user.getId());

    when(projectRepository.findById(eq(project.getId()))).thenReturn(Optional.of(project));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/projects/{id}", project.getId())
        .then()
        .statusCode(204);

    verify(projectRepository).remove(eq(project));
  }

  @Test
  public void should_403_if_not_author_delete_project() throws Exception {
    DateTime now = new DateTime();
    User anotherUser = new User("test@test.com", "test", "123123", "", "");

    Project project =
        new Project("Project", "Description", "Client", now, "active", anotherUser.getId());

    when(projectRepository.findById(eq(project.getId()))).thenReturn(Optional.of(project));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/projects/{id}", project.getId())
        .then()
        .statusCode(403);
  }

  private HashMap<String, Object> prepareUpdateParam(
      final String name,
      final String description,
      final String client,
      final String status) {
    return new HashMap<String, Object>() {
      {
        put(
            "project",
            new HashMap<String, Object>() {
              {
                put("name", name);
                put("description", description);
                put("client", client);
                put("status", status);
              }
            });
      }
    };
  }
}
