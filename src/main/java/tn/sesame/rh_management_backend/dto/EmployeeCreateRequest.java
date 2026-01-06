package tn.sesame.rh_management_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import tn.sesame.rh_management_backend.Enumerations.ContractType;
import tn.sesame.rh_management_backend.Enumerations.EmployeeDepartment;
import tn.sesame.rh_management_backend.Enumerations.EmployeeJobTitle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EmployeeCreateRequest(
        @NotBlank(message = "Employee number is required")
        @Pattern(regexp = "^EMP\\d{5}$", message = "Employee number must start with 'EMP' followed by 5 digits")
        String employeeNumber,

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z\\s-]+$", message = "First name can only contain letters, spaces, and hyphens")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z\\s-]+$", message = "Last name can only contain letters, spaces, and hyphens")
        String lastName,

        @NotNull(message = "Department is required")
        EmployeeDepartment department,

        @NotNull(message = "Job title is required")
        EmployeeJobTitle jobTitle,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @PastOrPresent(message = "Hire date cannot be in the future")
        LocalDate hireDate,

        @NotNull(message = "Contract type is required")
        ContractType contract,

        @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Salary format is invalid")
        BigDecimal salary,

        UUID userId,
        UUID managerId
) {

}
