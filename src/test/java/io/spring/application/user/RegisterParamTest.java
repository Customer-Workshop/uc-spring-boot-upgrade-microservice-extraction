package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RegisterParamTest {

  @Test
  public void should_create_register_param() {
    RegisterParam param = new RegisterParam("email@test.com", "username", "password");
    assertEquals("email@test.com", param.getEmail());
    assertEquals("username", param.getUsername());
    assertEquals("password", param.getPassword());
  }

  @Test
  public void should_create_empty_register_param() {
    RegisterParam param = new RegisterParam();
    assertNull(param.getEmail());
    assertNull(param.getUsername());
    assertNull(param.getPassword());
  }
}
