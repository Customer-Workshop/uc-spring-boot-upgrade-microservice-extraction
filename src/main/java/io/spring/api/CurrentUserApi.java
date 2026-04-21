package io.spring.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: Current user endpoints have been extracted to the User Service.
// These endpoints are now served by the user-service module on port 8081:
//   GET /user   -> user-service
//   PUT /user   -> user-service
// Remove this placeholder once the API gateway is configured to route to the User Service.
@RestController
@RequestMapping(path = "/user")
public class CurrentUserApi {}
