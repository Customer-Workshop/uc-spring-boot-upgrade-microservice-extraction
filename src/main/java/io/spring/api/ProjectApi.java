package io.spring.api;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.data.ProjectData;
import io.spring.application.project.ProjectCommandService;
import io.spring.application.project.ProjectQueryService;
import io.spring.application.project.UpdateProjectParam;
import io.spring.core.project.Project;
import io.spring.core.project.ProjectRepository;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/projects/{id}")
@AllArgsConstructor
public class ProjectApi {
  private ProjectQueryService projectQueryService;
  private ProjectRepository projectRepository;
  private ProjectCommandService projectCommandService;

  @GetMapping
  public ResponseEntity<?> project(
      @PathVariable("id") String id, @AuthenticationPrincipal User user) {
    return projectQueryService
        .findById(id)
        .map(projectData -> ResponseEntity.ok(projectResponse(projectData)))
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PutMapping
  public ResponseEntity<?> updateProject(
      @PathVariable("id") String id,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody UpdateProjectParam updateProjectParam) {
    return projectRepository
        .findById(id)
        .map(
            project -> {
              if (!project.getUserId().equals(user.getId())) {
                throw new NoAuthorizationException();
              }
              Project updatedProject =
                  projectCommandService.updateProject(project, updateProjectParam);
              return ResponseEntity.ok(
                  projectResponse(projectQueryService.findById(updatedProject.getId()).get()));
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping
  public ResponseEntity deleteProject(
      @PathVariable("id") String id, @AuthenticationPrincipal User user) {
    return projectRepository
        .findById(id)
        .map(
            project -> {
              if (!project.getUserId().equals(user.getId())) {
                throw new NoAuthorizationException();
              }
              projectRepository.remove(project);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Map<String, Object> projectResponse(ProjectData projectData) {
    return new HashMap<String, Object>() {
      {
        put("project", projectData);
      }
    };
  }
}
