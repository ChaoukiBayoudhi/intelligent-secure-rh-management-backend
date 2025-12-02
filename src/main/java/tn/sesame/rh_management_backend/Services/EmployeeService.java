package tn.sesame.rh_management_backend.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tn.sesame.rh_management_backend.Entities.Employee;
import tn.sesame.rh_management_backend.Repositories.EmployeeRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService{
    @Autowired
    private final EmployeeRepository employeeRepository;

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
}
