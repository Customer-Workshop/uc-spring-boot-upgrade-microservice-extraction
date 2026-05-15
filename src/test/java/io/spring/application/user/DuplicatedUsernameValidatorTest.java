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
  void should_return_true_when_username_is_null() {
    assertTrue(validator.isValid(null, context));
  }

  @Test
  void should_return_true_when_username_is_empty() {
    assertTrue(validator.isValid("", context));
  }

  @Test
  void should_return_true_when_username_not_taken() {
    when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

    assertTrue(validator.isValid("newuser", context));
  }

  @Test
  void should_return_false_when_username_already_taken() {
    User existingUser = new User("a@b.com", "takenuser", "pass", "", "");
    when(userRepository.findByUsername("takenuser")).thenReturn(Optional.of(existingUser));

    assertFalse(validator.isValid("takenuser", context));
  }
}
