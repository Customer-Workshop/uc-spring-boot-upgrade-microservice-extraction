package io.spring.userservice.repository;

import io.spring.userservice.domain.UserData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserReadService {
  UserData findById(@Param("id") String id);

  UserData findByUsername(@Param("username") String username);
}
