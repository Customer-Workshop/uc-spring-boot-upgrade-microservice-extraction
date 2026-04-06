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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@DgsComponent
public class UserMutation {

  private final UserRepository userRepository;
  private final PasswordEncoder encryptService;
  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserMutation(
      UserRepository userRepository,
      PasswordEncoder encryptService,
      RestTemplate restTemplate,
      @Value("${user-service.url}") String userServiceUrl) {
    this.userRepository = userRepository;
    this.encryptService = encryptService;
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
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

    restTemplate.postForObject(userServiceUrl + "/users", request, Map.class);

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

    currentUser.update(
        updateUserInput.getEmail(),
        updateUserInput.getUsername(),
        updateUserInput.getPassword(),
        updateUserInput.getBio(),
        updateUserInput.getImage());

    return DataFetcherResult.<UserPayload>newResult()
        .data(UserPayload.newBuilder().build())
        .localContext(currentUser)
        .build();
  }
}
