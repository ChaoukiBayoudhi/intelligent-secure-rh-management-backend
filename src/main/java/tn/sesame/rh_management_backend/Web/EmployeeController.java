package tn.sesame.rh_management_backend.Web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.sesame.rh_management_backend.Enumerations.EmployeeDepartment;
import tn.sesame.rh_management_backend.Services.EmployeeService;
import tn.sesame.rh_management_backend.dto.AddEmployeeDto;
import tn.sesame.rh_management_backend.dto.EmployeeDto;

import java.util.Set;

import static org.springframework.security.authorization.AuthorityReactiveAuthorizationManager.hasAnyRole;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {
    //@Autowired
    //optional with the use of @RequiredArgsConstructor
    private final EmployeeService employeeService;

    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @GetMapping("/{department}")
    public ResponseEntity<Set<EmployeeDto>> getEmployeesByDepartment(@PathVariable EmployeeDepartment department){
        return employeeService.getEmployeesByDepartment(department);
    }
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
    @PostMapping("/")
    public ResponseEntity<?> addEmployee(@Validated @RequestBody AddEmployeeDto request)
    {
        return employeeService.addEmployee(request);
    }

}
