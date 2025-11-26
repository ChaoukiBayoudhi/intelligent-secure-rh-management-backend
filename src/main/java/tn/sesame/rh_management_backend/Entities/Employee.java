package tn.sesame.rh_management_backend.Entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import tn.sesame.rh_management_backend.Enumerations.ContractType;
import tn.sesame.rh_management_backend.Enumerations.EmployeeDepartment;
import tn.sesame.rh_management_backend.Enumerations.EmployeeJobTitle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "of")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude={"user", "subordinates", "documents", "manager"})
@EqualsAndHashCode(of="employeeNumber")
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @NonNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("employee")
    User user;

    @NonNull
    @Column(name = "employee_number", length = 8, unique = true)
    @Pattern(regexp = "^EMP\\d{5}$", message = "Employee number must start with 'EMP' followed by 5 digits")
    String employeeNumber;

    @NonNull
    @NotBlank(message = "First name cannot be empty")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    //Prevents numbers/symbols in names
    @Pattern(regexp = "^[a-zA-Z\\s-]+$", message = "First name can only contain letters, spaces, and hyphens")
    String firstName;

    @NonNull
    @NotBlank(message = "Last name cannot be empty")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s-]+$", message = "Last name can only contain letters, spaces, and hyphens")
    String lastName;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Department is required")
    EmployeeDepartment department;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Job title is required")
    EmployeeJobTitle jobTitle;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "Hire date cannot be in the future")
            //by default, LocalDate is in UTC
            //hireDate has by default the system date
    LocalDate hireDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Contract type is required")
    ContractType contract;

    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
    //Limits 10 digits before decimal, 2 after
    @Digits(integer = 10, fraction = 2, message = "Salary format is invalid (expected: 12345.67)")
    BigDecimal salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    Employee manager;

    @OneToMany(mappedBy = "employee",fetch = FetchType.LAZY)
    @JsonIgnoreProperties("manager")
    Set<Employee> subordinates=new HashSet<>();

    @OneToMany(mappedBy = "employee",fetch = FetchType.LAZY)
    @JsonIgnoreProperties("employee")
    Set<HRDocument> documents=new HashSet<>();

}
