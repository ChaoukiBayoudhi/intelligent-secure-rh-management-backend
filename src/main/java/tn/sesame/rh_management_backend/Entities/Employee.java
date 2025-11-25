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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Accessors(fluent = true)//calling the getters and setters without using the prefixes get and set
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(staticName = "of")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude={"user"})
@EqualsAndHashCode(of="employeeNumber")
@Table(name = "employees",
        uniqueConstraints = @UniqueConstraint(columnNames = {"email"}))

public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @NonNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="user_id")
    User user;
    @NonNull
    @Column(name = "employee_number",length = 8,unique = true)
    String employeeNumber;
    @NonNull
    String firstName;
    @NonNull
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

    //reflexive relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    Employee manager;

    @OneToMany(mappedBy = "manager",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    Set<Employee> subordinates=new HashSet<>();

    @OneToMany(mappedBy = "employee",
            fetch = FetchType.LAZY)
    Set<HRDocument> documents=new HashSet<>();

}
