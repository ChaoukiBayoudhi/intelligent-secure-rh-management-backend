package tn.sesame.rh_management_backend.Validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SalaryRangeValidator.class)
public @interface ValidSalaryRange {
    String message() default "Salary is not within the valid range for this job title";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
