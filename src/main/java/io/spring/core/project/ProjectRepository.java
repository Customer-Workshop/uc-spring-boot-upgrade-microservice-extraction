package io.spring.core.project;

import java.util.Optional;

public interface ProjectRepository {

  void save(Project project);

  Optional<Project> findById(String id);

  void remove(Project project);
}
