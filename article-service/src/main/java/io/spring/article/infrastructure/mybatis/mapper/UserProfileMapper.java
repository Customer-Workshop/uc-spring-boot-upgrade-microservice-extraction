package io.spring.article.infrastructure.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserProfileMapper {
  void upsert(
      @Param("id") String id,
      @Param("username") String username,
      @Param("bio") String bio,
      @Param("image") String image);

  String findById(@Param("id") String id);
}
