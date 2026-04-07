package io.spring.application.project;

import io.spring.Util;
import io.spring.core.project.Project;
import io.spring.core.project.ProjectRepository;
import io.spring.core.user.User;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@AllArgsConstructor
public class ProjectCommandService {

  private ProjectRepository projectRepository;

  public Project createProject(@Valid NewProjectParam newProjectParam, User creator) {
    DateTime startDate =
        Util.isEmpty(newProjectParam.getStartDate())
            ? null
            : DateTime.parse(newProjectParam.getStartDate());
    Project project =
        new Project(
            newProjectParam.getName(),
            newProjectParam.getDescription(),
            newProjectParam.getClient(),
            startDate,
            newProjectParam.getStatus(),
            creator.getId());
    projectRepository.save(project);
    return project;
  }

  public Project updateProject(Project project, @Valid UpdateProjectParam updateProjectParam) {
    DateTime startDate =
        Util.isEmpty(updateProjectParam.getStartDate())
            ? null
            : DateTime.parse(updateProjectParam.getStartDate());
    project.update(
        updateProjectParam.getName(),
        updateProjectParam.getDescription(),
        updateProjectParam.getClient(),
        startDate,
        updateProjectParam.getStatus());
    projectRepository.save(project);
    return project;
  }
}
