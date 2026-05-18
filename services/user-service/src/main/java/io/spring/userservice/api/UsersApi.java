package io.spring.userservice.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.userservice.api.exception.InvalidAuthenticationException;
import io.spring.userservice.domain.User;
import io.spring.userservice.domain.UserData;
import io.spring.userservice.domain.UserWithToken;
import io.spring.userservice.repository.UserMapper;
import io.spring.userservice.repository.UserReadService;
import io.spring.userservice.service.JwtService;
import io.spring.userservice.service.UserService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UsersApi {
  private UserMapper userMapper;
  private UserReadService userReadService;
  private PasswordEncoder passwordEncoder;
  private JwtService jwtService;
  private UserService userService;

  @RequestMapping(path = "/users", method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> createUser(
      @Valid @RequestBody RegisterParam registerParam) {
    User user =
        userService.createUser(
            registerParam.getEmail(), registerParam.getUsername(), registerParam.getPassword());
    UserData userData = userReadService.findById(user.getId());
    return ResponseEntity.status(201)
        .body(userResponse(new UserWithToken(userData, jwtService.toToken(user))));
  }

  @RequestMapping(path = "/users/login", method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> userLogin(
      @Valid @RequestBody LoginParam loginParam) {
    Optional<User> optional = userMapper.findByEmail(loginParam.getEmail());
    if (optional.isPresent()
        && passwordEncoder.matches(loginParam.getPassword(), optional.get().getPassword())) {
      UserData userData = userReadService.findById(optional.get().getId());
      return ResponseEntity.ok(
          userResponse(new UserWithToken(userData, jwtService.toToken(optional.get()))));
    } else {
      throw new InvalidAuthenticationException();
    }
  }

  private Map<String, Object> userResponse(UserWithToken userWithToken) {
    return new HashMap<String, Object>() {
      {
        put("user", userWithToken);
      }
    };
  }
}

@Getter
@JsonRootName("user")
@NoArgsConstructor
class RegisterParam {
  @NotBlank(message = "can't be empty")
  @Email(message = "should be an email")
  private String email;

  @NotBlank(message = "can't be empty")
  private String username;

  @NotBlank(message = "can't be empty")
  private String password;
}

@Getter
@JsonRootName("user")
@NoArgsConstructor
class LoginParam {
  @NotBlank(message = "can't be empty")
  @Email(message = "should be an email")
  private String email;

  @NotBlank(message = "can't be empty")
  private String password;
}
