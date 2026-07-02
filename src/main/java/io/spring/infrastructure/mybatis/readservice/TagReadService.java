package io.spring.infrastructure.mybatis.readservice;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/** MyBatis mapper for reading all article tag names. */
@Mapper
public interface TagReadService {

  /** Returns all distinct tag names. */
  List<String> all();
}
