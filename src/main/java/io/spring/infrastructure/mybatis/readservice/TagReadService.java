package io.spring.infrastructure.mybatis.readservice;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/** Reads tag names from the tags table. */
@Mapper
public interface TagReadService {
  /** Returns all tag names. */
  List<String> all();
}
