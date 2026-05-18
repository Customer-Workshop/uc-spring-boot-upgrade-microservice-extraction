package io.spring.userservice.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.userservice.domain.User;
import io.spring.userservice.domain.UserData;
import io.spring.userservice.domain.UserWithToken;
import io.spring.userservice.repository.UserReadService;
import io.spring.userservice.service.UserService;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/user")
@AllArgsConstructor
public class CurrentUserApi {

  private UserReadService userReadService;
  private UserService userService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> currentUser(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader(value = "Authorization") String authorization) {
    UserData userData = userReadService.findById(currentUser.getId());
    return ResponseEntity.ok(
        userResponse(new UserWithToken(userData, authorization.split(" ")[1])));
  }

  @PutMapping
  public ResponseEntity<Map<String, Object>> updateProfile(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader("Authorization") String token,
      @Valid @RequestBody UpdateUserParam updateUserParam) {
    userService.updateUser(
        currentUser,
        updateUserParam.getEmail(),
        updateUserParam.getUsername(),
        updateUserParam.getPassword(),
        updateUserParam.getBio(),
        updateUserParam.getImage());
    UserData userData = userReadService.findById(currentUser.getId());
    return ResponseEntity.ok(userResponse(new UserWithToken(userData, token.split(" ")[1])));
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
@AllArgsConstructor
@Builder
class UpdateUserParam {
  @Builder.Default
  @Email(message = "should be an email")
  private String email = "";

  @Builder.Default private String password = "";
  @Builder.Default private String username = "";
  @Builder.Default private String bio = "";
  @Builder.Default private String image = "";
}
