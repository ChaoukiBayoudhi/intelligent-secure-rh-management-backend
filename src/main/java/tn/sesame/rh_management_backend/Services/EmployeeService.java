package tn.sesame.rh_management_backend.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.sesame.rh_management_backend.Entities.Employee;
import tn.sesame.rh_management_backend.Enumerations.EmployeeDepartment;
import tn.sesame.rh_management_backend.Repositories.EmployeeRepository;
import tn.sesame.rh_management_backend.dto.EmployeeDto;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService{
    @Autowired
    private final EmployeeRepository employeeRepository;

    @Transactional
    public ResponseEntity<?> addEmployee(Employee employee){
        Optional<Employee> result=employeeRepository.findAll().stream()
                .filter(e->e.employeeNumber().equals(employee.employeeNumber()))
                .findFirst();
        if(result.isPresent())
            return ResponseEntity.badRequest().body("Employee with this number already exists");
        //return ResponseEntity.ok(employeeRepository.save(employee));
        Employee emp=employeeRepository.save(employee);
        return new ResponseEntity<>(emp, HttpStatus.CREATED);
    }

    public ResponseEntity<Set<EmployeeDto>> getEmployeesByDepartment(EmployeeDepartment department){
        return null;
    }
    public EmployeeDto convertToDto(Employee e)
    {
        return EmployeeDto.builder()
                .id(e.id())
                .firstName(e.firstName())
                .lastName(e.lastName())
                .employeeNumber(e.employeeNumber())
                .salary(e.salary())
                .hireDate(e.hireDate())
                .jobTitle(e.jobTitle())
                .contract(e.contract())
                .build();
    }
}
