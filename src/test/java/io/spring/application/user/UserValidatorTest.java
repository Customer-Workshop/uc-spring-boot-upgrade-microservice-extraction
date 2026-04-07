package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class UserValidatorTest {

  private DuplicatedEmailValidator emailValidator;
  private DuplicatedUsernameValidator usernameValidator;
  private UpdateUserValidator updateUserValidator;
  private UserRepository userRepository;
  private ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    context = mock(ConstraintValidatorContext.class);

    emailValidator = new DuplicatedEmailValidator();
    ReflectionTestUtils.setField(emailValidator, "userRepository", userRepository);

    usernameValidator = new DuplicatedUsernameValidator();
    ReflectionTestUtils.setField(usernameValidator, "userRepository", userRepository);

    updateUserValidator = new UpdateUserValidator();
    ReflectionTestUtils.setField(updateUserValidator, "userRepository", userRepository);
  }

  // === DuplicatedEmailValidator tests ===

  @Test
  void should_return_true_when_email_is_null() {
    assertTrue(emailValidator.isValid(null, context));
  }

  @Test
  void should_return_true_when_email_is_empty() {
    assertTrue(emailValidator.isValid("", context));
  }

  @Test
  void should_return_true_when_email_not_duplicated() {
    when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

    assertTrue(emailValidator.isValid("new@example.com", context));
  }

  @Test
  void should_return_false_when_email_is_duplicated() {
    User existing = new User("existing@example.com", "existinguser", "pass", "", "");
    when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existing));

    assertFalse(emailValidator.isValid("existing@example.com", context));
  }

  // === DuplicatedUsernameValidator tests ===

  @Test
  void should_return_true_when_username_is_null() {
    assertTrue(usernameValidator.isValid(null, context));
  }

  @Test
  void should_return_true_when_username_is_empty() {
    assertTrue(usernameValidator.isValid("", context));
  }

  @Test
  void should_return_true_when_username_not_duplicated() {
    when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

    assertTrue(usernameValidator.isValid("newuser", context));
  }

  @Test
  void should_return_false_when_username_is_duplicated() {
    User existing = new User("user@example.com", "existinguser", "pass", "", "");
    when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existing));

    assertFalse(usernameValidator.isValid("existinguser", context));
  }

  // === UpdateUserValidator tests ===

  @Test
  void should_return_true_when_email_and_username_are_available() {
    User targetUser = new User("old@example.com", "olduser", "pass", "", "");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@example.com")
            .username("newuser")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

    assertTrue(updateUserValidator.isValid(command, context));
  }

  @Test
  void should_return_true_when_email_and_username_belong_to_same_user() {
    User targetUser = new User("same@example.com", "sameuser", "pass", "", "");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("same@example.com")
            .username("sameuser")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    when(userRepository.findByEmail("same@example.com")).thenReturn(Optional.of(targetUser));
    when(userRepository.findByUsername("sameuser")).thenReturn(Optional.of(targetUser));

    assertTrue(updateUserValidator.isValid(command, context));
  }

  @Test
  void should_return_false_when_email_belongs_to_another_user() {
    User targetUser = new User("old@example.com", "olduser", "pass", "", "");
    User otherUser = new User("taken@example.com", "otheruser", "pass", "", "");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("taken@example.com")
            .username("newuser")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(otherUser));
    when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

    ConstraintValidatorContext.ConstraintViolationBuilder builder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);

    assertFalse(updateUserValidator.isValid(command, context));
    verify(context).disableDefaultConstraintViolation();
  }

  @Test
  void should_return_false_when_username_belongs_to_another_user() {
    User targetUser = new User("old@example.com", "olduser", "pass", "", "");
    User otherUser = new User("other@example.com", "takenuser", "pass", "", "");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@example.com")
            .username("takenuser")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("takenuser")).thenReturn(Optional.of(otherUser));

    ConstraintValidatorContext.ConstraintViolationBuilder builder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);

    assertFalse(updateUserValidator.isValid(command, context));
    verify(context).disableDefaultConstraintViolation();
  }

  @Test
  void should_return_false_when_both_email_and_username_taken_by_others() {
    User targetUser = new User("old@example.com", "olduser", "pass", "", "");
    User otherUser1 = new User("taken@example.com", "other1", "pass", "", "");
    User otherUser2 = new User("other@example.com", "takenuser", "pass", "", "");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("taken@example.com")
            .username("takenuser")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(otherUser1));
    when(userRepository.findByUsername("takenuser")).thenReturn(Optional.of(otherUser2));

    ConstraintValidatorContext.ConstraintViolationBuilder builder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);

    assertFalse(updateUserValidator.isValid(command, context));
    verify(context).disableDefaultConstraintViolation();
    // Should have two constraint violations built
    verify(context, times(2)).buildConstraintViolationWithTemplate(anyString());
  }
}
