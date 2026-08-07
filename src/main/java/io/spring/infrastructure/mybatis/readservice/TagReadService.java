package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.TagStatsData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagReadService {
  List<String> all();

  List<TagStatsData> stats();
}
