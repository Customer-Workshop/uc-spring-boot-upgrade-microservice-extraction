package io.spring.articleservice.repository;

import io.spring.articleservice.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserReadService {
  User findById(@Param("id") String id);
}
