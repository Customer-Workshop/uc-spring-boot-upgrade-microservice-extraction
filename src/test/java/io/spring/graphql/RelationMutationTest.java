package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class RelationMutationTest extends GraphqlTestBase {
  private final User user = GraphqlTestFixtures.user();
  private final User target = GraphqlTestFixtures.user();

  @Test
  void followsExistingUser() {
    UserRepository users = mock(UserRepository.class);
    ProfileQueryService profiles = mock(ProfileQueryService.class);
    when(users.findByUsername("target")).thenReturn(Optional.of(target));
    when(profiles.findByUsername("target", user))
        .thenReturn(Optional.of(new ProfileData(target.getId(), "target", "bio", "image", true)));
    RelationMutation mutation = new RelationMutation(users, profiles);
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertEquals("target", mutation.follow("target").getProfile().getUsername());
    }
  }

  @Test
  void unfollowsExistingRelationAndRejectsMissingRelation() {
    UserRepository users = mock(UserRepository.class);
    ProfileQueryService profiles = mock(ProfileQueryService.class);
    when(users.findByUsername("target")).thenReturn(Optional.of(target));
    when(profiles.findByUsername("target", user))
        .thenReturn(Optional.of(new ProfileData(target.getId(), "target", "bio", "image", false)));
    RelationMutation mutation = new RelationMutation(users, profiles);
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(users.findRelation(user.getId(), target.getId())).thenReturn(Optional.empty());
      assertThrows(ResourceNotFoundException.class, () -> mutation.unfollow("target"));
      when(users.findRelation(user.getId(), target.getId()))
          .thenReturn(
              Optional.of(new io.spring.core.user.FollowRelation(user.getId(), target.getId())));
      assertEquals("target", mutation.unfollow("target").getProfile().getUsername());
    }
  }

  @Test
  void rejectsUnauthenticatedAndMissingTarget() {
    UserRepository users = mock(UserRepository.class);
    RelationMutation mutation = new RelationMutation(users, mock(ProfileQueryService.class));
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      assertThrows(AuthenticationException.class, () -> mutation.follow("target"));
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(users.findByUsername("missing")).thenReturn(Optional.empty());
      assertThrows(ResourceNotFoundException.class, () -> mutation.follow("missing"));
    }
  }
}
