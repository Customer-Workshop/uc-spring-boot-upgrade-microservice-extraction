package io.spring.application.project;

import io.spring.core.project.Project;
import io.spring.core.project.ProjectRepository;
import io.spring.core.user.User;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@AllArgsConstructor
public class ProjectCommandService {

  private ProjectRepository projectRepository;

  public Project createProject(@Valid NewProjectParam newProjectParam, User creator) {
    Project project =
        new Project(
            newProjectParam.getName(),
            newProjectParam.getDescription(),
            newProjectParam.getClient(),
            newProjectParam.getStartDate(),
            newProjectParam.getStatus(),
            creator.getId());
    projectRepository.save(project);
    return project;
  }

  public Project updateProject(Project project, @Valid UpdateProjectParam updateProjectParam) {
    project.update(
        updateProjectParam.getName(),
        updateProjectParam.getDescription(),
        updateProjectParam.getClient(),
        updateProjectParam.getStartDate(),
        updateProjectParam.getStatus());
    projectRepository.save(project);
    return project;
  }
}
