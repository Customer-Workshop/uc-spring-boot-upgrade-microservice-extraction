package io.spring.application.project;

import io.spring.application.data.ProjectData;
import io.spring.infrastructure.mybatis.readservice.ProjectReadService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProjectQueryService {
  private ProjectReadService projectReadService;

  public Optional<ProjectData> findById(String id) {
    ProjectData projectData = projectReadService.findById(id);
    if (projectData == null) {
      return Optional.empty();
    }
    return Optional.of(projectData);
  }

  public List<ProjectData> findAll() {
    return projectReadService.findAll();
  }

  public int countAll() {
    return projectReadService.countAll();
  }
}
