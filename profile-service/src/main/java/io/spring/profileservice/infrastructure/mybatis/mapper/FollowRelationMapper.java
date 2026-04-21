package io.spring.profileservice.infrastructure.mybatis.mapper;

import io.spring.profileservice.core.FollowRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FollowRelationMapper {

  void saveRelation(@Param("followRelation") FollowRelation followRelation);

  FollowRelation findRelation(@Param("userId") String userId, @Param("targetId") String targetId);

  void deleteRelation(@Param("followRelation") FollowRelation followRelation);
}
