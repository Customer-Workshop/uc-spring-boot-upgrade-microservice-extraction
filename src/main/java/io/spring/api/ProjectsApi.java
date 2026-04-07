package io.spring.api;

import io.spring.application.project.NewProjectParam;
import io.spring.application.project.ProjectCommandService;
import io.spring.application.project.ProjectQueryService;
import io.spring.core.project.Project;
import io.spring.core.user.User;
import java.util.HashMap;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/projects")
@AllArgsConstructor
public class ProjectsApi {
  private ProjectCommandService projectCommandService;
  private ProjectQueryService projectQueryService;

  @PostMapping
  public ResponseEntity createProject(
      @Valid @RequestBody NewProjectParam newProjectParam, @AuthenticationPrincipal User user) {
    Project project = projectCommandService.createProject(newProjectParam, user);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("project", projectQueryService.findById(project.getId()).get());
          }
        });
  }

  @GetMapping
  public ResponseEntity getProjects(@AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("projects", projectQueryService.findAll());
            put("projectsCount", projectQueryService.countAll());
          }
        });
  }
}
