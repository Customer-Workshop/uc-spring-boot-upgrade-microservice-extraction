package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.UserData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Reads projected user data from the users table. */
@Mapper
public interface UserReadService {

  /** Finds projected user data by username. */
  UserData findByUsername(@Param("username") String username);

  /** Finds projected user data by identifier. */
  UserData findById(@Param("id") String id);
}
