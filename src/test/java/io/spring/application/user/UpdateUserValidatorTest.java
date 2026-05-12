package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UpdateUserValidatorTest {

  @Mock private UserRepository userRepository;
  @Mock private ConstraintValidatorContext context;

  @InjectMocks private UpdateUserValidator validator;

  private User targetUser;

  @BeforeEach
  public void setUp() {
    targetUser = new User("test@test.com", "testuser", "123", "bio", "image");
  }

  @Test
  public void should_return_true_when_no_conflicts() {
    UpdateUserParam param =
        UpdateUserParam.builder().email("new@test.com").username("newuser").build();
    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    when(userRepository.findByEmail(eq("new@test.com"))).thenReturn(Optional.empty());
    when(userRepository.findByUsername(eq("newuser"))).thenReturn(Optional.empty());

    assertTrue(validator.isValid(command, context));
  }

  @Test
  public void should_return_true_when_same_user() {
    UpdateUserParam param =
        UpdateUserParam.builder().email("test@test.com").username("testuser").build();
    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    when(userRepository.findByEmail(eq("test@test.com"))).thenReturn(Optional.of(targetUser));
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(targetUser));

    assertTrue(validator.isValid(command, context));
  }

  @Test
  public void should_return_false_when_email_taken() {
    User anotherUser = new User("other@test.com", "other", "123", "", "");
    UpdateUserParam param =
        UpdateUserParam.builder().email("other@test.com").username("newuser").build();
    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    when(userRepository.findByEmail(eq("other@test.com"))).thenReturn(Optional.of(anotherUser));
    when(userRepository.findByUsername(eq("newuser"))).thenReturn(Optional.empty());

    ConstraintValidatorContext.ConstraintViolationBuilder builder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext
        nodeBuilder =
            mock(
                ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext
                    .class);
    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);

    assertFalse(validator.isValid(command, context));
    verify(context).disableDefaultConstraintViolation();
  }

  @Test
  public void should_return_false_when_username_taken() {
    User anotherUser = new User("other@test.com", "other", "123", "", "");
    UpdateUserParam param =
        UpdateUserParam.builder().email("new@test.com").username("other").build();
    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    when(userRepository.findByEmail(eq("new@test.com"))).thenReturn(Optional.empty());
    when(userRepository.findByUsername(eq("other"))).thenReturn(Optional.of(anotherUser));

    ConstraintValidatorContext.ConstraintViolationBuilder builder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext
        nodeBuilder =
            mock(
                ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext
                    .class);
    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);

    assertFalse(validator.isValid(command, context));
    verify(context).disableDefaultConstraintViolation();
  }

  @Test
  public void should_return_false_when_both_taken() {
    User emailUser = new User("taken@test.com", "u1", "123", "", "");
    User nameUser = new User("u2@test.com", "taken", "123", "", "");
    UpdateUserParam param =
        UpdateUserParam.builder().email("taken@test.com").username("taken").build();
    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    when(userRepository.findByEmail(eq("taken@test.com"))).thenReturn(Optional.of(emailUser));
    when(userRepository.findByUsername(eq("taken"))).thenReturn(Optional.of(nameUser));

    ConstraintValidatorContext.ConstraintViolationBuilder builder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext
        nodeBuilder =
            mock(
                ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext
                    .class);
    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);

    assertFalse(validator.isValid(command, context));
  }
}
