package io.spring.api;

import org.springframework.web.bind.annotation.RestController;

// TODO: User registration and login endpoints have been extracted to the User Service.
// These endpoints are now served by the user-service module on port 8081:
//   POST /users        -> user-service
//   POST /users/login  -> user-service
// Remove this placeholder once the API gateway is configured to route to the User Service.
@RestController
public class UsersApi {}
