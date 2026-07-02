package io.spring.application;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/** Read-side service for querying user data by identifier. */
@Service
@AllArgsConstructor
public class UserQueryService {
  private UserReadService userReadService;

  /**
   * Finds a user's read-model data by unique identifier.
   *
   * @param id the user's unique identifier
   * @return the user data, or empty if not found
   */
  public Optional<UserData> findById(String id) {
    return Optional.ofNullable(userReadService.findById(id));
  }
}
