package io.spring.infrastructure.mybatis.readservice;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TagReadService {
  List<String> all();

  List<String> findTagsByArticleId(@Param("articleId") String articleId);
}
