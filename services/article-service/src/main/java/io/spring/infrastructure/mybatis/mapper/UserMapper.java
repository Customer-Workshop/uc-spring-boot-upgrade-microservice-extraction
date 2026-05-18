package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
  void insert(@Param("user") User user);

  User findById(@Param("id") String id);

  User findByUsername(@Param("username") String username);

  User findByEmail(@Param("email") String email);

  void update(@Param("user") User user);

  void saveRelation(@Param("followRelation") FollowRelation followRelation);

  FollowRelation findRelation(@Param("userId") String userId, @Param("targetId") String targetId);

  void deleteRelation(@Param("followRelation") FollowRelation followRelation);
}
