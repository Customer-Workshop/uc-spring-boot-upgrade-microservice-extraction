package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Maps user and follow-relation persistence operations to SQL statements. */
@Mapper
public interface UserMapper {
  /** Inserts a user row. */
  void insert(@Param("user") User user);

  /** Finds a user by username. */
  User findByUsername(@Param("username") String username);

  /** Finds a user by email address. */
  User findByEmail(@Param("email") String email);

  /** Finds a user by identifier. */
  User findById(@Param("id") String id);

  /** Updates a user row. */
  void update(@Param("user") User user);

  /** Finds a follow relationship between two users. */
  FollowRelation findRelation(@Param("userId") String userId, @Param("targetId") String targetId);

  /** Inserts a follow relationship. */
  void saveRelation(@Param("followRelation") FollowRelation followRelation);

  /** Deletes a follow relationship. */
  void deleteRelation(@Param("followRelation") FollowRelation followRelation);
}
