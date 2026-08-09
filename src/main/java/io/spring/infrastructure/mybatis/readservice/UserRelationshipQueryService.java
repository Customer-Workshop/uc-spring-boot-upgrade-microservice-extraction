package io.spring.infrastructure.mybatis.readservice;

import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Reads follow relationships from the follows table. */
@Mapper
public interface UserRelationshipQueryService {
  /** Returns whether one user follows another. */
  boolean isUserFollowing(
      @Param("userId") String userId, @Param("anotherUserId") String anotherUserId);

  /** Returns supplied user identifiers followed by the given user. */
  Set<String> followingAuthors(@Param("userId") String userId, @Param("ids") List<String> ids);

  /** Returns identifiers of all users followed by the given user. */
  List<String> followedUsers(@Param("userId") String userId);
}
