package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
  void upsert(@Param("user") User user);

  User findById(@Param("id") String id);
}
