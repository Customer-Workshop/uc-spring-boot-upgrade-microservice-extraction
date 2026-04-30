package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DuplicatedUsernameValidatorTest {

  @Mock private UserRepository userRepository;
  @Mock private ConstraintValidatorContext context;

  private DuplicatedUsernameValidator validator;

  @BeforeEach
  void setUp() {
    validator = new DuplicatedUsernameValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
  }

  @Test
  void should_return_true_when_username_is_null() {
    assertTrue(validator.isValid(null, context));
  }

  @Test
  void should_return_true_when_username_is_empty() {
    assertTrue(validator.isValid("", context));
  }

  @Test
  void should_return_true_when_username_not_taken() {
    when(userRepository.findByUsername(eq("newuser"))).thenReturn(Optional.empty());

    assertTrue(validator.isValid("newuser", context));
  }

  @Test
  void should_return_false_when_username_is_taken() {
    User existing = new User("test@test.com", "taken", "pass", "", "");
    when(userRepository.findByUsername(eq("taken"))).thenReturn(Optional.of(existing));

    assertFalse(validator.isValid("taken", context));
  }
}
