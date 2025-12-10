package tn.sesame.rh_management_backend.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.sesame.rh_management_backend.Entities.Employee;
import tn.sesame.rh_management_backend.Entities.User;
import tn.sesame.rh_management_backend.Enumerations.EmployeeDepartment;
import tn.sesame.rh_management_backend.Repositories.EmployeeRepository;
import tn.sesame.rh_management_backend.Repositories.UserRepository;
import tn.sesame.rh_management_backend.dto.AddEmployeeDto;
import tn.sesame.rh_management_backend.dto.EmployeeDto;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService{
    //@Autowired
    //optional with the use of @RequiredArgsConstructor
    private final EmployeeRepository employeeRepository;
    //@Autowired
    private final UserRepository userRepository;

    @Transactional
    public ResponseEntity<?> addEmployee(AddEmployeeDto employeeRequest){
        Optional<Employee> result=employeeRepository.findByEmployeeNumber(employeeRequest.employeeNumber());
        if(result.isPresent())
            return ResponseEntity.badRequest().body("Employee with this number already exists");
        //return ResponseEntity.ok(employeeRepository.save(employee));
        //verify if the user is already created
        Optional<User> rs=userRepository.findById(employeeRequest.user().getId());

        User user= rs.isEmpty()?userRepository.save(employeeRequest.user()):rs.get();

        //get the manager if it exists
        Employee manager=null;
        Optional<Employee> res=employeeRepository.findById(employeeRequest.managerId());
        if(res.isPresent())
            manager=res.get();

        //convert from AddEmployeeDto to Employee
        Employee employee=Employee.builder()
                .firstName(employeeRequest.firstName())
                .lastName(employeeRequest.lastName())
                .salary(employeeRequest.salary())
                .hireDate((employeeRequest.hireDate()))
                .employeeNumber(employeeRequest.employeeNumber())
                .contract(employeeRequest.contract())
                .department(employeeRequest.department())
                .jobTitle(employeeRequest.jobTitle())
                .user(user)
                .manager(manager)
                .build();

        Employee emp=employeeRepository.save(employee);
        return new ResponseEntity<>(emp, HttpStatus.CREATED);
    }
    @Transactional(readOnly=true)
    public ResponseEntity<Set<EmployeeDto>> getEmployeesByDepartment(EmployeeDepartment department){
        Set<EmployeeDto> employees=employeeRepository.findByDepartment(department)
                .stream()
                .map(this::convertToDto)

                .collect(Collectors.toSet());
    return ResponseEntity.ok(employees);
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
