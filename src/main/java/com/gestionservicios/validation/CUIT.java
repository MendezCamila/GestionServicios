package com.gestionservicios.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = CUITValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface CUIT {
    String message() default "CUIT inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
