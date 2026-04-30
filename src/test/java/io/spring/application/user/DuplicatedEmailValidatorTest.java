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
class DuplicatedEmailValidatorTest {

  @Mock private UserRepository userRepository;
  @Mock private ConstraintValidatorContext context;

  private DuplicatedEmailValidator validator;

  @BeforeEach
  void setUp() {
    validator = new DuplicatedEmailValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
  }

  @Test
  void should_return_true_when_email_is_null() {
    assertTrue(validator.isValid(null, context));
  }

  @Test
  void should_return_true_when_email_is_empty() {
    assertTrue(validator.isValid("", context));
  }

  @Test
  void should_return_true_when_email_not_taken() {
    when(userRepository.findByEmail(eq("new@test.com"))).thenReturn(Optional.empty());

    assertTrue(validator.isValid("new@test.com", context));
  }

  @Test
  void should_return_false_when_email_is_taken() {
    User existing = new User("taken@test.com", "user", "pass", "", "");
    when(userRepository.findByEmail(eq("taken@test.com"))).thenReturn(Optional.of(existing));

    assertFalse(validator.isValid("taken@test.com", context));
  }
}
