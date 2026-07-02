package io.spring.application.user;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.validation.Constraint;
import javax.validation.Payload;

/** Validation constraint that rejects email addresses already registered by another user. */
@Constraint(validatedBy = DuplicatedEmailValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface DuplicatedEmailConstraint {
  String message() default "duplicated email";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
