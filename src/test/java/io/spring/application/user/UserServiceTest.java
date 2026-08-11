package io.spring.application.user;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  private static final String DEFAULT_IMAGE = "https://static.productionready.io/smiley.jpg";

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private UserService userService;
  private User user;

  @BeforeEach
  public void setUp() {
    userService = new UserService(userRepository, DEFAULT_IMAGE, passwordEncoder);
    user = new User("a@test.com", "a", "encoded", "bio", "image");
  }

  @Test
  public void should_create_user_with_default_image() {
    when(passwordEncoder.encode(eq("123"))).thenReturn("encoded");

    User created = userService.createUser(new RegisterParam("a@test.com", "a", "123"));

    Assertions.assertEquals("a@test.com", created.getEmail());
    Assertions.assertEquals("a", created.getUsername());
    Assertions.assertEquals("encoded", created.getPassword());
    Assertions.assertEquals(DEFAULT_IMAGE, created.getImage());
    Assertions.assertEquals("", created.getBio());
    verify(userRepository).save(created);
  }

  @Test
  public void should_update_user_success() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("new")
            .bio("new bio")
            .image("new image")
            .password("new password")
            .build();

    userService.updateUser(new UpdateUserCommand(user, param));

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    Assertions.assertEquals("new@test.com", captor.getValue().getEmail());
    Assertions.assertEquals("new", captor.getValue().getUsername());
    Assertions.assertEquals("new bio", captor.getValue().getBio());
  }

  @Test
  public void should_create_user_with_special_characters_in_profile() {
    when(passwordEncoder.encode(eq("p@ss w?rd\\'\"🔐"))).thenReturn("encoded");

    User created =
        userService.createUser(
            new RegisterParam("jose+tag@exämple.co.uk", "José_🚀", "p@ss w?rd\\'\"🔐"));

    Assertions.assertEquals("jose+tag@exämple.co.uk", created.getEmail());
    Assertions.assertEquals("José_🚀", created.getUsername());
    Assertions.assertEquals("encoded", created.getPassword());
  }

  @Test
  public void should_update_user_with_special_characters_in_bio() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .bio("Ünïcödé bio — <b>html</b> & \"quotes\"\nsecond line 🎉")
            .image("https://example.com/a b?x=1&y=2")
            .build();

    userService.updateUser(new UpdateUserCommand(user, param));

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    Assertions.assertEquals(
        "Ünïcödé bio — <b>html</b> & \"quotes\"\nsecond line 🎉", captor.getValue().getBio());
    Assertions.assertEquals("https://example.com/a b?x=1&y=2", captor.getValue().getImage());
  }

  @Test
  public void should_reject_email_with_invalid_special_characters() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    Assertions.assertEquals(
        1,
        validator
            .validateProperty(UpdateUserParam.builder().email("not an email 🚀").build(), "email")
            .size());
    Assertions.assertTrue(
        validator
            .validateProperty(
                UpdateUserParam.builder().email("jose+tag@exämple.co.uk").build(), "email")
            .isEmpty());
  }

  @Test
  public void should_expose_register_param_fields() {
    RegisterParam param = new RegisterParam("a@test.com", "a", "123");

    Assertions.assertEquals("a@test.com", param.getEmail());
    Assertions.assertEquals("a", param.getUsername());
    Assertions.assertEquals("123", param.getPassword());
  }

  @Test
  public void should_accept_update_when_email_and_username_belong_to_target_user() {
    UpdateUserValidator validator = new UpdateUserValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    when(userRepository.findByEmail(eq("a@test.com"))).thenReturn(Optional.of(user));
    when(userRepository.findByUsername(eq("a"))).thenReturn(Optional.of(user));
    UpdateUserCommand command =
        new UpdateUserCommand(
            user, UpdateUserParam.builder().email("a@test.com").username("a").build());

    Assertions.assertTrue(
        validator.isValid(command, mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS)));
  }

  @Test
  public void should_reject_update_with_duplicated_email_and_username() {
    UpdateUserValidator validator = new UpdateUserValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    User anotherUser = new User("taken@test.com", "taken", "123", "", "");
    when(userRepository.findByEmail(eq("taken@test.com"))).thenReturn(Optional.of(anotherUser));
    when(userRepository.findByUsername(eq("taken"))).thenReturn(Optional.of(anotherUser));
    UpdateUserCommand command =
        new UpdateUserCommand(
            user, UpdateUserParam.builder().email("taken@test.com").username("taken").build());
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS);

    Assertions.assertFalse(validator.isValid(command, context));
    verify(context).disableDefaultConstraintViolation();
    verify(context).buildConstraintViolationWithTemplate("email already exist");
    verify(context).buildConstraintViolationWithTemplate("username already exist");
  }

  @Test
  public void should_reject_duplicated_email_on_register() {
    DuplicatedEmailValidator validator = new DuplicatedEmailValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    when(userRepository.findByEmail(eq("a@test.com"))).thenReturn(Optional.of(user));

    Assertions.assertFalse(validator.isValid("a@test.com", null));
  }

  @Test
  public void should_accept_empty_email_on_register() {
    DuplicatedEmailValidator validator = new DuplicatedEmailValidator();

    Assertions.assertTrue(validator.isValid("", null));
    Assertions.assertTrue(validator.isValid(null, null));
  }

  @Test
  public void should_reject_duplicated_username_on_register() {
    DuplicatedUsernameValidator validator = new DuplicatedUsernameValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    when(userRepository.findByUsername(eq("a"))).thenReturn(Optional.of(user));

    Assertions.assertFalse(validator.isValid("a", null));
  }

  @Test
  public void should_accept_new_username_on_register() {
    DuplicatedUsernameValidator validator = new DuplicatedUsernameValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    when(userRepository.findByUsername(eq("new"))).thenReturn(Optional.empty());

    Assertions.assertTrue(validator.isValid("new", null));
  }
}
