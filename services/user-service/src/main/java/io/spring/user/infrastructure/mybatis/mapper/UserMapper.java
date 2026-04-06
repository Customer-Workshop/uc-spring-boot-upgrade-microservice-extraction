package io.spring.user.infrastructure.mybatis.mapper;

import io.spring.user.core.FollowRelation;
import io.spring.user.core.User;
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
