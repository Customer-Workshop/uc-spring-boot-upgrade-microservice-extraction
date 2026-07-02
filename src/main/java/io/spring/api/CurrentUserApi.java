package io.spring.api;

import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UpdateUserParam;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
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

/**
 * REST controller for the currently authenticated user's account.
 *
 * <p>Provides endpoints to retrieve and update the current user's profile information.
 */
@RestController
@RequestMapping(path = "/user")
@AllArgsConstructor
public class CurrentUserApi {

  private UserQueryService userQueryService;
  private UserService userService;

  /**
   * Retrieves the currently authenticated user's profile.
   *
   * @param currentUser the authenticated user resolved from the JWT
   * @param authorization the Authorization header containing the Bearer token
   * @return the user data with token wrapped in a {@code {"user": ...}} envelope
   */
  @GetMapping
  public ResponseEntity currentUser(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader(value = "Authorization") String authorization) {
    UserData userData = userQueryService.findById(currentUser.getId()).get();
    return ResponseEntity.ok(
        userResponse(new UserWithToken(userData, authorization.split(" ")[1])));
  }

  /**
   * Updates the currently authenticated user's profile.
   *
   * @param currentUser the authenticated user resolved from the JWT
   * @param token the Authorization header containing the Bearer token
   * @param updateUserParam the fields to update (email, username, password, bio, image)
   * @return the updated user data with token wrapped in a {@code {"user": ...}} envelope
   */
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
