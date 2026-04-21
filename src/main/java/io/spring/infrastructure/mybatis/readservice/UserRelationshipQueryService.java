package io.spring.infrastructure.mybatis.readservice;

// TODO: This query service has been extracted to the Profile Service (profile-service/).
// The Profile Service exposes internal APIs for follow relationship queries:
//   GET  /api/internal/follows/is-following?userId=xxx&targetId=yyy
//   POST /api/internal/follows/following-authors (body: {userId, authorIds})
//   GET  /api/internal/follows/followed-users/{userId}
// Replace usages of this interface with HTTP calls to the Profile Service.
// See: profile-service/src/main/java/io/spring/profileservice/api/InternalFollowApi.java

import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRelationshipQueryService {
  boolean isUserFollowing(
      @Param("userId") String userId, @Param("anotherUserId") String anotherUserId);

  Set<String> followingAuthors(@Param("userId") String userId, @Param("ids") List<String> ids);

  List<String> followedUsers(@Param("userId") String userId);
}
