package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@DgsComponent
public class UserMutation {

  private final UserRepository userRepository;
  private final PasswordEncoder encryptService;
  private final RestTemplate restTemplate;
  private final String userServiceUrl;
  private final HttpServletRequest httpServletRequest;

  public UserMutation(
      UserRepository userRepository,
      PasswordEncoder encryptService,
      RestTemplate restTemplate,
      @Value("${user-service.url}") String userServiceUrl,
      HttpServletRequest httpServletRequest) {
    this.userRepository = userRepository;
    this.encryptService = encryptService;
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
    this.httpServletRequest = httpServletRequest;
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.CreateUser)
  public DataFetcherResult<UserResult> createUser(@InputArgument("input") CreateUserInput input) {
    Map<String, Object> body = new HashMap<>();
    Map<String, String> userFields = new HashMap<>();
    userFields.put("email", input.getEmail());
    userFields.put("username", input.getUsername());
    userFields.put("password", input.getPassword());
    body.put("user", userFields);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

    try {
      restTemplate.postForObject(userServiceUrl + "/users", request, Map.class);
    } catch (HttpClientErrorException e) {
      throw new InvalidAuthenticationException();
    }

    Optional<User> userOpt = userRepository.findByEmail(input.getEmail());
    if (userOpt.isPresent()) {
      return DataFetcherResult.<UserResult>newResult()
          .data(UserPayload.newBuilder().build())
          .localContext(userOpt.get())
          .build();
    }

    return DataFetcherResult.<UserResult>newResult()
        .data(UserPayload.newBuilder().build())
        .build();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.Login)
  public DataFetcherResult<UserPayload> login(
      @InputArgument("password") String password, @InputArgument("email") String email) {
    Optional<User> optional = userRepository.findByEmail(email);
    if (optional.isPresent() && encryptService.matches(password, optional.get().getPassword())) {
      return DataFetcherResult.<UserPayload>newResult()
          .data(UserPayload.newBuilder().build())
          .localContext(optional.get())
          .build();
    } else {
      throw new InvalidAuthenticationException();
    }
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UpdateUser)
  public DataFetcherResult<UserPayload> updateUser(
      @InputArgument("changes") UpdateUserInput updateUserInput) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof AnonymousAuthenticationToken
        || authentication.getPrincipal() == null) {
      return null;
    }
    io.spring.core.user.User currentUser =
        (io.spring.core.user.User) authentication.getPrincipal();

    // Forward the update to user-service via REST
    Map<String, Object> body = new HashMap<>();
    Map<String, String> userFields = new HashMap<>();
    if (updateUserInput.getEmail() != null) userFields.put("email", updateUserInput.getEmail());
    if (updateUserInput.getUsername() != null) userFields.put("username", updateUserInput.getUsername());
    if (updateUserInput.getPassword() != null) userFields.put("password", updateUserInput.getPassword());
    if (updateUserInput.getBio() != null) userFields.put("bio", updateUserInput.getBio());
    if (updateUserInput.getImage() != null) userFields.put("image", updateUserInput.getImage());
    body.put("user", userFields);

    HttpHeaders headers = buildAuthHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

    try {
      restTemplate.exchange(
          userServiceUrl + "/user", HttpMethod.PUT, request, Map.class);
    } catch (HttpClientErrorException e) {
      throw new InvalidAuthenticationException();
    }

    // Re-fetch updated user from user-service
    Optional<User> updatedUser = userRepository.findById(currentUser.getId());
    User resolvedUser = updatedUser.orElse(currentUser);

    return DataFetcherResult.<UserPayload>newResult()
        .data(UserPayload.newBuilder().build())
        .localContext(resolvedUser)
        .build();
  }

  private HttpHeaders buildAuthHeaders() {
    HttpHeaders headers = new HttpHeaders();
    String authHeader = httpServletRequest.getHeader("Authorization");
    if (authHeader != null) {
      headers.set("Authorization", authHeader);
    }
    return headers;
  }
}
