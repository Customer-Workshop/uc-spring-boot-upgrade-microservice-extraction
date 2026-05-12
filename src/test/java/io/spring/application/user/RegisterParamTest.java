package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RegisterParamTest {

  @Test
  void should_create_param_with_all_args() {
    RegisterParam param = new RegisterParam("test@test.com", "user", "password");

    assertEquals("test@test.com", param.getEmail());
    assertEquals("user", param.getUsername());
    assertEquals("password", param.getPassword());
  }

  @Test
  void should_create_param_with_no_args() {
    RegisterParam param = new RegisterParam();

    assertNull(param.getEmail());
    assertNull(param.getUsername());
    assertNull(param.getPassword());
  }
}
