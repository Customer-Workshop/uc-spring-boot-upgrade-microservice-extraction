package io.spring.infrastructure.repository;

import io.spring.core.project.Project;
import io.spring.core.project.ProjectRepository;
import io.spring.infrastructure.mybatis.mapper.ProjectMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MyBatisProjectRepository implements ProjectRepository {
  private ProjectMapper projectMapper;

  public MyBatisProjectRepository(ProjectMapper projectMapper) {
    this.projectMapper = projectMapper;
  }

  @Override
  @Transactional
  public void save(Project project) {
    if (projectMapper.findById(project.getId()) == null) {
      projectMapper.insert(project);
    } else {
      projectMapper.update(project);
    }
  }

  @Override
  public Optional<Project> findById(String id) {
    return Optional.ofNullable(projectMapper.findById(id));
  }

  @Override
  public void remove(Project project) {
    projectMapper.delete(project.getId());
  }
}
