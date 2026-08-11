package io.spring;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.executable.ExecutableValidator;

public class ValidationTestHelper {

  public static class Param {
    @NotBlank(message = "can't be empty")
    private String email;

    @NotBlank(message = "can't be empty")
    private String username;

    public Param() {}

    public Param(String email, String username) {
      this.email = email;
      this.username = username;
    }

    public String getEmail() {
      return email;
    }

    public String getUsername() {
      return username;
    }
  }

  public static class Service {
    public void register(@Valid Param param) {}
  }

  /** Builds a real ConstraintViolationException with a simple `field` property path. */
  public static ConstraintViolationException beanViolations() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<Param>> violations = validator.validate(new Param("", ""));
    return new ConstraintViolationException(violations);
  }

  /**
   * Builds a real ConstraintViolationException with a method parameter property path such as
   * `register.param.email`.
   */
  public static ConstraintViolationException methodViolations() {
    ExecutableValidator validator =
        Validation.buildDefaultValidatorFactory().getValidator().forExecutables();
    try {
      Set<ConstraintViolation<Service>> violations =
          validator.validateParameters(
              new Service(),
              Service.class.getMethod("register", Param.class),
              new Object[] {new Param("", "")});
      return new ConstraintViolationException(violations);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
  }
}
