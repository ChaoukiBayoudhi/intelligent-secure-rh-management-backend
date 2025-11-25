package tn.sesame.rh_management_backend.Entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import tn.sesame.rh_management_backend.Enumerations.ContractType;
import tn.sesame.rh_management_backend.Enumerations.EmployeeDepartment;
import tn.sesame.rh_management_backend.Enumerations.EmployeeJobTitle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Accessors(fluent = true)//calling the getters and setters without using the prefixes get and set
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(staticName = "of")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude={"password","mfaSecret"})
@EqualsAndHashCode(of="email")
@Table(name = "employees",
        uniqueConstraints = @UniqueConstraint(columnNames = {"email"})

public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @OneToOne
    @JoinColumn(name="user_id")
    User user;
    @Column(name = "employee_number",length = 8,unique = true)
    String employeeNumber;
    String firstName;
    String lastName;
    @Enumerated(EnumType.STRING)
    EmployeeDepartment department;
    @Enumerated(EnumType.STRING)
    EmployeeJobTitle jobTitle;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    ContractType contract;
    BigDecimal salary;
    @Column(name = "manager_id")
    UUID managerId;

}
