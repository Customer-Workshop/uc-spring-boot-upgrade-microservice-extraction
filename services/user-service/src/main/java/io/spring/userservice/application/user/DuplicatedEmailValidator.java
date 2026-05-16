package io.spring.userservice.application.user;

import io.spring.userservice.core.user.UserRepository;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class DuplicatedEmailValidator
    implements ConstraintValidator<DuplicatedEmailConstraint, String> {

  @Autowired private UserRepository userRepository;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return (value == null || value.isEmpty()) || !userRepository.findByEmail(value).isPresent();
  }
}
