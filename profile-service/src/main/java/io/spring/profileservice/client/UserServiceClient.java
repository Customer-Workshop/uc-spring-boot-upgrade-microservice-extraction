package io.spring.profileservice.client;

import io.spring.profileservice.application.data.UserData;
import java.util.Optional;

/**
 * HTTP client interface for communicating with the User Service. In the microservice architecture,
 * user data is owned by the User Service. The Profile Service calls these methods to resolve
 * usernames and user IDs into user data.
 */
public interface UserServiceClient {

  Optional<UserData> findByUsername(String username);

  Optional<UserData> findById(String id);
}
