package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RegisterParamTest {

  @Test
  void should_create_with_all_args() {
    RegisterParam param = new RegisterParam("email@test.com", "user", "pass");
    assertEquals("email@test.com", param.getEmail());
    assertEquals("user", param.getUsername());
    assertEquals("pass", param.getPassword());
  }

  @Test
  void should_create_with_no_args() {
    RegisterParam param = new RegisterParam();
    assertNull(param.getEmail());
    assertNull(param.getUsername());
    assertNull(param.getPassword());
  }
}
