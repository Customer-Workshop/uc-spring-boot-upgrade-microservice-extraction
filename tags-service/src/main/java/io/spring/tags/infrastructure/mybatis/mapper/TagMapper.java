package io.spring.tags.infrastructure.mybatis.mapper;

import io.spring.tags.domain.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TagMapper {

  Tag findByName(@Param("name") String name);

  Tag findById(@Param("id") String id);

  void insert(@Param("tag") Tag tag);

  void insertArticleTagRelation(
      @Param("articleId") String articleId, @Param("tagId") String tagId);
}
