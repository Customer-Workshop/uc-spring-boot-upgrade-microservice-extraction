package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class DuplicatedEmailValidatorTest {

  private DuplicatedEmailValidator validator;
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    validator = new DuplicatedEmailValidator();
    userRepository = mock(UserRepository.class);
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
  }

  @Test
  void should_return_true_when_email_is_null() {
    assertTrue(validator.isValid(null, null));
  }

  @Test
  void should_return_true_when_email_is_empty() {
    assertTrue(validator.isValid("", null));
  }

  @Test
  void should_return_true_when_email_not_found() {
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
