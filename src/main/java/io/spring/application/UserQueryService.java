package io.spring.application;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/** Provides user data reads. */
@Service
@AllArgsConstructor
public class UserQueryService {
  private UserReadService userReadService;

  /** Finds projected user data by identifier. */
  public Optional<UserData> findById(String id) {
    return Optional.ofNullable(userReadService.findById(id));
  }
}
