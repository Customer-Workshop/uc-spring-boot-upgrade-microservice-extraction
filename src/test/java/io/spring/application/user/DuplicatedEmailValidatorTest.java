package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DuplicatedEmailValidatorTest {

  @Mock private UserRepository userRepository;
  @Mock private ConstraintValidatorContext context;

  @InjectMocks private DuplicatedEmailValidator validator;

  @Test
  void should_return_true_when_email_is_unique() {
    when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());

    assertTrue(validator.isValid("new@test.com", context));
  }

  @Test
  void should_return_false_when_email_already_exists() {
    User existing = new User("existing@test.com", "user", "pass", "", "");
    when(userRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(existing));

    assertFalse(validator.isValid("existing@test.com", context));
  }

  @Test
  void should_return_true_when_email_is_null() {
    assertTrue(validator.isValid(null, context));
  }

  @Test
  void should_return_true_when_email_is_empty() {
    assertTrue(validator.isValid("", context));
  }
}
