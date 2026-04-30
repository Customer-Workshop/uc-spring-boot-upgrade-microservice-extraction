package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class RegisterParamTest {

  @Test
  public void should_create_with_all_args() {
    RegisterParam param = new RegisterParam("test@test.com", "testuser", "password");

    assertEquals("test@test.com", param.getEmail());
    assertEquals("testuser", param.getUsername());
    assertEquals("password", param.getPassword());
  }

  @Test
  public void should_create_with_no_args() {
    RegisterParam param = new RegisterParam();
    assertNotNull(param);
    assertNull(param.getEmail());
    assertNull(param.getUsername());
    assertNull(param.getPassword());
  }
}
