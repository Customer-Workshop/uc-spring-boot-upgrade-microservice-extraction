package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.data.ProjectData;
import io.spring.application.project.ProjectCommandService;
import io.spring.application.project.ProjectQueryService;
import io.spring.core.project.Project;
import java.util.Arrays;
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

@WebMvcTest({ProjectsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ProjectsApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ProjectQueryService projectQueryService;

  @MockBean private ProjectCommandService projectCommandService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_create_project_success() throws Exception {
    String name = "Website Redesign";
    String description = "Complete overhaul of the company website";
    String client = "Acme Corp";
    String status = "active";
    DateTime startDate = new DateTime();

    Map<String, Object> param = prepareParam(name, description, client, status);

    Project project = new Project(name, description, client, startDate, status, user.getId());

    ProjectData projectData =
        new ProjectData(
            project.getId(),
            name,
            description,
            client,
            startDate,
            status,
            user.getId(),
            new DateTime(),
            new DateTime());

    when(projectCommandService.createProject(any(), any())).thenReturn(project);
    when(projectQueryService.findById(any())).thenReturn(Optional.of(projectData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/projects")
        .then()
        .statusCode(200)
        .body("project.name", equalTo(name))
        .body("project.description", equalTo(description))
        .body("project.client", equalTo(client))
        .body("project.status", equalTo(status));

    verify(projectCommandService).createProject(any(), any());
  }

  @Test
  public void should_get_error_message_with_empty_name() throws Exception {
    Map<String, Object> param = prepareParam("", "description", "client", "active");

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/projects")
        .then()
        .statusCode(422);
  }

  @Test
  public void should_get_error_message_with_empty_status() throws Exception {
    Map<String, Object> param = prepareParam("Project Name", "description", "client", "");

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/projects")
        .then()
        .statusCode(422);
  }

  @Test
  public void should_get_all_projects_success() throws Exception {
    DateTime now = new DateTime();
    ProjectData project1 =
        new ProjectData(
            "id1", "Project 1", "Desc 1", "Client 1", now, "active", user.getId(), now, now);
    ProjectData project2 =
        new ProjectData(
            "id2", "Project 2", "Desc 2", "Client 2", now, "completed", user.getId(), now, now);

    when(projectQueryService.findAll()).thenReturn(Arrays.asList(project1, project2));
    when(projectQueryService.countAll()).thenReturn(2);

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/projects")
        .then()
        .statusCode(200)
        .body("projectsCount", equalTo(2));
  }

  private HashMap<String, Object> prepareParam(
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
