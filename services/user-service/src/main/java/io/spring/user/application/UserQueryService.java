package io.spring.user.application;

import io.spring.shared.dto.UserData;
import io.spring.user.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserQueryService {
  private UserReadService userReadService;

  public Optional<UserData> findById(String id) {
    return Optional.ofNullable(userReadService.findById(id));
  }
}
