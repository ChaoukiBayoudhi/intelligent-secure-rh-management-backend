package tn.sesame.rh_management_backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.sesame.rh_management_backend.Entities.Employee;
import tn.sesame.rh_management_backend.Enumerations.EmployeeDepartment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByEmployeeNumber(String employeeNumber);
    boolean existsByEmployeeNumber(String employeeNumber);
    List<Employee> findByDepartment(EmployeeDepartment department);
    List<Employee> findByDepartment(String department);
}
