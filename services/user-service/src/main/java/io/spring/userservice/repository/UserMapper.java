package io.spring.userservice.repository;

import io.spring.userservice.domain.FollowRelation;
import io.spring.userservice.domain.User;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
  void insert(@Param("user") User user);

  void update(@Param("user") User user);

  Optional<User> findById(@Param("id") String id);

  Optional<User> findByUsername(@Param("username") String username);

  Optional<User> findByEmail(@Param("email") String email);

  void saveRelation(@Param("followRelation") FollowRelation followRelation);

  Optional<FollowRelation> findRelation(
      @Param("userId") String userId, @Param("targetId") String targetId);

  void deleteRelation(@Param("followRelation") FollowRelation followRelation);
}
