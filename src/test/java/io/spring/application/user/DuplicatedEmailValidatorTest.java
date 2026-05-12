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
public class DuplicatedEmailValidatorTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private DuplicatedEmailValidator validator;

  @Test
  void should_return_true_when_email_is_null() {
    assertTrue(validator.isValid(null, null));
  }

  @Test
  void should_return_true_when_email_is_empty() {
    assertTrue(validator.isValid("", null));
  }

  @Test
  void should_return_true_when_email_does_not_exist() {
    when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
    assertTrue(validator.isValid("new@test.com", null));
  }

  @Test
  void should_return_false_when_email_already_exists() {
    User existing = new User("existing@test.com", "user", "pass", "", "");
    when(userRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(existing));
    assertFalse(validator.isValid("existing@test.com", null));
  }
}
