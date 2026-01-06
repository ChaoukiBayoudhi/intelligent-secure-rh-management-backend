package tn.sesame.rh_management_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import tn.sesame.rh_management_backend.Enumerations.ContractType;
import tn.sesame.rh_management_backend.Enumerations.EmployeeDepartment;
import tn.sesame.rh_management_backend.Enumerations.EmployeeJobTitle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private UUID id;
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private EmployeeDepartment department;
    private EmployeeJobTitle jobTitle;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;
    private ContractType contract;
    private BigDecimal salary;
    private UUID managerId;
    private String managerName;
    private UserDTO user;
}
