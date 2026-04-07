package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.project.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectMapper {
  void insert(@Param("project") Project project);

  Project findById(@Param("id") String id);

  void update(@Param("project") Project project);

  void delete(@Param("id") String id);
}
