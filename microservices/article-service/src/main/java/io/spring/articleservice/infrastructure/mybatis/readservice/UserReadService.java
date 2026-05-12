package io.spring.articleservice.infrastructure.mybatis.readservice;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserReadService {

  Object findByUsername(@Param("username") String username);

  Object findById(@Param("id") String id);
}
