package tn.sesame.rh_management_backend.Web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.sesame.rh_management_backend.Services.EmployeeService;
import tn.sesame.rh_management_backend.dto.EmployeeCreateRequest;
import tn.sesame.rh_management_backend.dto.EmployeeDto;
import tn.sesame.rh_management_backend.dto.EmployeeUpdateRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        EmployeeDto employee = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        List<EmployeeDto> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable UUID id) {
        EmployeeDto employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/number/{employeeNumber}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<EmployeeDto> getEmployeeByNumber(@PathVariable String employeeNumber) {
        EmployeeDto employee = employeeService.getEmployeeByNumber(employeeNumber);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByDepartment(@PathVariable String department) {
        List<EmployeeDto> employees = employeeService.getEmployeesByDepartment(department);
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeUpdateRequest request
    ) {
        EmployeeDto employee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteEmployee(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee deleted successfully");
    }
}
