package io.spring.infrastructure.mybatis.readservice;

import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** MyBatis mapper for querying user follow relationships. */
@Mapper
public interface UserRelationshipQueryService {

  /** Checks whether one user follows another. */
  boolean isUserFollowing(
      @Param("userId") String userId, @Param("anotherUserId") String anotherUserId);

  /** Returns the subset of the given user IDs that the current user follows. */
  Set<String> followingAuthors(@Param("userId") String userId, @Param("ids") List<String> ids);

  /** Returns all user IDs that the given user follows. */
  List<String> followedUsers(@Param("userId") String userId);
}
