package org.reddot15.be_stockmanager.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.reddot15.be_stockmanager.validation.validator.MatchValidator;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = MatchValidator.class)
public @interface Match {
	String[] fields();

	String message() default "These field must match.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
