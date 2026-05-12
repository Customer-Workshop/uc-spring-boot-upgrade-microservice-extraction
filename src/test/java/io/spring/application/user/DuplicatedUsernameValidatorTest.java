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
public class DuplicatedUsernameValidatorTest {

  @Mock private UserRepository userRepository;
  @Mock private ConstraintValidatorContext context;

  @InjectMocks private DuplicatedUsernameValidator validator;

  @Test
  void should_return_true_when_username_is_unique() {
    when(userRepository.findByUsername("uniqueuser")).thenReturn(Optional.empty());

    assertTrue(validator.isValid("uniqueuser", context));
  }

  @Test
  void should_return_false_when_username_already_exists() {
    User existingUser = new User("existing@test.com", "existinguser", "pass", "", "");
    when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

    assertFalse(validator.isValid("existinguser", context));
  }

  @Test
  void should_return_true_when_username_is_null() {
    assertTrue(validator.isValid(null, context));
  }

  @Test
  void should_return_true_when_username_is_empty() {
    assertTrue(validator.isValid("", context));
  }
}
