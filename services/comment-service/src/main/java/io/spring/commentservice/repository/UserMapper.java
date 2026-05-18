package io.spring.commentservice.repository;

import io.spring.commentservice.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
  User findById(@Param("id") String id);
}
