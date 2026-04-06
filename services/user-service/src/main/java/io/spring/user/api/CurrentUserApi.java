package io.spring.user.api;

import io.spring.shared.dto.UserData;
import io.spring.shared.dto.UserWithToken;
import io.spring.user.application.UserQueryService;
import io.spring.user.application.user.UpdateUserCommand;
import io.spring.user.application.user.UpdateUserParam;
import io.spring.user.application.user.UserService;
import io.spring.user.core.User;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
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

  private UserQueryService userQueryService;
  private UserService userService;

  @GetMapping
  public ResponseEntity currentUser(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader(value = "Authorization") String authorization) {
    UserData userData = userQueryService.findById(currentUser.getId()).get();
    return ResponseEntity.ok(
        userResponse(new UserWithToken(userData, authorization.split(" ")[1])));
  }

  @PutMapping
  public ResponseEntity updateProfile(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader("Authorization") String token,
      @Valid @RequestBody UpdateUserParam updateUserParam) {
    userService.updateUser(new UpdateUserCommand(currentUser, updateUserParam));
    UserData userData = userQueryService.findById(currentUser.getId()).get();
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
