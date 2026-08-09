package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {
  private final UserRepository repository = mock(UserRepository.class);
  private final PasswordEncoder encoder = mock(PasswordEncoder.class);
  private final UserService service = new UserService(repository, "default-image", encoder);

  @Test
  void createsAndUpdatesUser() {
    RegisterParam register = new RegisterParam("email@test.com", "user", "plain");
    when(encoder.encode("plain")).thenReturn("encoded");
    User created = service.createUser(register);
    assertEquals("encoded", created.getPassword());
    assertEquals("default-image", created.getImage());
    verify(repository).save(created);

    UpdateUserParam param = UpdateUserParam.builder().email("new@test.com").username("new").bio("bio").image("image").build();
    service.updateUser(new UpdateUserCommand(created, param));
    assertEquals("new@test.com", created.getEmail());
    verify(repository, times(2)).save(created);
  }

  @Test
  void validatesDuplicateEmailAndUsernameCombinations() {
    User target = new User("target@test.com", "target", "p", "", "");
    UpdateUserParam param = UpdateUserParam.builder().email("email").username("username").build();
    UpdateUserCommand command = new UpdateUserCommand(target, param);
    UpdateUserValidator validator = new UpdateUserValidator();
    org.springframework.test.util.ReflectionTestUtils.setField(validator, "userRepository", repository);
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
    when(repository.findByEmail("email")).thenReturn(Optional.empty());
    when(repository.findByUsername("username")).thenReturn(Optional.empty());
    assertEquals(true, validator.isValid(command, context));
    User other = new User("other@test.com", "other", "p", "", "");
    when(repository.findByEmail("email")).thenReturn(Optional.of(other));
    when(repository.findByUsername("username")).thenReturn(Optional.of(other));
    ConstraintValidatorContext.ConstraintViolationBuilder builder =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
    ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext node =
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
    when(builder.addPropertyNode(any())).thenReturn(node);
    when(node.addConstraintViolation()).thenReturn(context);
    assertEquals(false, validator.isValid(command, context));
    verify(context).disableDefaultConstraintViolation();
  }
}
