package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DuplicatedUsernameValidatorTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private DuplicatedUsernameValidator validator;

  @Test
  void should_return_true_when_username_is_null() {
    assertTrue(validator.isValid(null, null));
  }

  @Test
  void should_return_true_when_username_is_empty() {
    assertTrue(validator.isValid("", null));
  }

  @Test
  void should_return_true_when_username_does_not_exist() {
    when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
    assertTrue(validator.isValid("newuser", null));
  }

  @Test
  void should_return_false_when_username_already_exists() {
    User existing = new User("e@e.com", "existinguser", "pass", "", "");
    when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existing));
    assertFalse(validator.isValid("existinguser", null));
  }
}
