package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.TagData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TagReadService {
  List<String> all();

  List<TagData> findAll();

  TagData findById(@Param("id") String id);

  List<TagData> findByArticleId(@Param("articleId") String articleId);
}
