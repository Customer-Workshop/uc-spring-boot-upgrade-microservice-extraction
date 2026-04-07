package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.ProjectData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectReadService {
  ProjectData findById(@Param("id") String id);

  List<ProjectData> findAll();

  int countAll();
}
