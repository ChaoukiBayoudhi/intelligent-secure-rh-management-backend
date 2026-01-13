package tn.sesame.rh_management_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import tn.sesame.rh_management_backend.Enumerations.ContractType;
import tn.sesame.rh_management_backend.Enumerations.EmployeeDepartment;
import tn.sesame.rh_management_backend.Enumerations.EmployeeJobTitle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

/**
 * Employee Data Transfer Object
 * 
 * This DTO represents employee data for API responses.
 * It includes all necessary employee information including manager and user details.
 */
@Builder
public record EmployeeDto(
        UUID id,
        String employeeNumber,
        String firstName,
        String lastName,
        EmployeeDepartment department,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate hireDate,
        BigDecimal salary,
        EmployeeJobTitle jobTitle,
        ContractType contract,
        UUID managerId,
        String managerName,
        UserDto user
) {
}
