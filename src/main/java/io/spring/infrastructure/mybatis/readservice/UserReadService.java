package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.UserData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** MyBatis mapper for read-side user queries. */
@Mapper
public interface UserReadService {

  /** Finds user read-model data by username. */
  UserData findByUsername(@Param("username") String username);

  /** Finds user read-model data by unique identifier. */
  UserData findById(@Param("id") String id);
}
