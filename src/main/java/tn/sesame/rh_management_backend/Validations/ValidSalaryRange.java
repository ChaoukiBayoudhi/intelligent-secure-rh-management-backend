package tn.sesame.rh_management_backend.Validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation interface used to validate that an employee's salary falls within the valid range
 * determined by their job title. This annotation is applied at the class level and leverages
 * the {@code SalaryRangeValidator} class to perform the validation logic.
 *
 * Constraints:
 * - If the `jobTitle` or `salary` fields are null, this validation is bypassed, allowing other
 *   annotations (e.g., {@code @NotNull}) to handle null validation.
 * - Supports custom salary ranges based on employee job titles. For example:
 *   - Sales Representative's salary must be between a predefined minimum and maximum range.
 *   - HR Manager's salary must not exceed a predefined upper limit.
 *   - Senior Engineer's salary must not exceed a predefined upper limit.
 *
 * Attributes:
 * - {@code message}: Error message returned when the validation fails.
 * - {@code groups}: Group(s) of constraints to which this validation belongs.
 * - {@code payload}: Payload type(s) associated with the constraint.
 *
 * This annotation should be applied to classes and is typically used in combination with
 * a JPA entity representing an employee or similar domain objects.
 *
 * Example Scenarios:
 * - A Sales Representative must have a salary between 3000 and 10000.
 * - An HR Manager's salary must not exceed 100000.
 * - A Senior Engineer's salary must not exceed 1000000.
 *
 * Note: This validation assumes that valid ranges for salaries have been predefined for
 * various job titles and are enforced dynamically by the {@code SalaryRangeValidator}.
 *
 * Usage:
 * Apply this annotation to a class representing an employee to ensure the salary is
 * appropriately constrained according to the business rules.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SalaryRangeValidator.class)
public @interface ValidSalaryRange {
    String message() default "Salary is not within the valid range for this job title";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
