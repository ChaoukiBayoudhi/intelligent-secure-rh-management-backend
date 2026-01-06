package tn.sesame.rh_management_backend.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.sesame.rh_management_backend.Entities.Employee;
import tn.sesame.rh_management_backend.Entities.User;
import tn.sesame.rh_management_backend.Repositories.EmployeeRepository;
import tn.sesame.rh_management_backend.Repositories.UserRepository;
import tn.sesame.rh_management_backend.dto.EmployeeCreateRequest;
import tn.sesame.rh_management_backend.dto.EmployeeDTO;
import tn.sesame.rh_management_backend.dto.EmployeeUpdateRequest;
import tn.sesame.rh_management_backend.dto.UserDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    @Transactional
    public EmployeeDTO createEmployee(EmployeeCreateRequest request) {
        // Check if employee number already exists
        if (employeeRepository.findByEmployeeNumber(request.employeeNumber()).isPresent()) {
            throw new tn.sesame.rh_management_backend.exceptions.ConflictException("Employee number already exists");
        }

        // Get user if userId is provided
        User user = null;
        if (request.userId() != null) {
            user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("User not found"));
        }

        // Get manager if managerId is provided
        Employee manager = null;
        if (request.managerId() != null) {
            manager = employeeRepository.findById(request.managerId())
                    .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("Manager not found"));
        }

        Employee employee = Employee.builder()
                .user(user)
                .employeeNumber(request.employeeNumber())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .department(request.department())
                .jobTitle(request.jobTitle())
                .hireDate(request.hireDate() != null ? request.hireDate() : LocalDate.now())
                .contract(request.contract())
                .salary(request.salary())
                .manager(manager)
                .build();

        employee = employeeRepository.save(employee);
        return convertToDTO(employee);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'HR_MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR_MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("Employee not found"));

        // Check if user can view this employee
        checkEmployeeAccess(employee);

        return convertToDTO(employee);
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR_MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeByNumber(String employeeNumber) {
        Employee employee = employeeRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("Employee not found"));

        // Check if user can view this employee
        checkEmployeeAccess(employee);

        return convertToDTO(employee);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'HR_MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    @Transactional
    public EmployeeDTO updateEmployee(UUID id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("Employee not found"));

        if (request.getFirstName() != null) {
            employee.firstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.lastName(request.getLastName());
        }
        if (request.getDepartment() != null) {
            employee.department(request.getDepartment());
        }
        if (request.getJobTitle() != null) {
            employee.jobTitle(request.getJobTitle());
        }
        if (request.getHireDate() != null) {
            employee.hireDate(request.getHireDate());
        }
        if (request.getContract() != null) {
            employee.contract(request.getContract());
        }
        if (request.getSalary() != null) {
            employee.salary(request.getSalary());
        }
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("Manager not found"));
            employee.manager(manager);
        }

        employee = employeeRepository.save(employee);
        return convertToDTO(employee);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteEmployee(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("Employee not found"));
        employeeRepository.delete(employee);
    }

    private void checkEmployeeAccess(Employee employee) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        // Admins and HR Managers can access all employees
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                              a.getAuthority().equals("ROLE_HR_MANAGER"))) {
            return;
        }

        // Regular employees can only access their own data
        if (employee.user() != null && !employee.user().getEmail().equals(currentUserEmail)) {
            throw new tn.sesame.rh_management_backend.exceptions.ForbiddenException("Access denied");
        }
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO.EmployeeDTOBuilder builder = EmployeeDTO.builder()
                .id(employee.id())
                .employeeNumber(employee.employeeNumber())
                .firstName(employee.firstName())
                .lastName(employee.lastName())
                .department(employee.department())
                .jobTitle(employee.jobTitle())
                .hireDate(employee.hireDate())
                .contract(employee.contract())
                .salary(employee.salary());

        if (employee.manager() != null) {
            builder.managerId(employee.manager().id())
                   .managerName(employee.manager().firstName() + " " + employee.manager().lastName());
        }

        if (employee.user() != null) {
            builder.user(new UserDTO(
                    employee.user().getId(),
                    employee.user().getEmail(),
                    employee.user().getRole(),
                    employee.user().isMfaEnabled(),
                    employee.user().isEmailVerified(),
                    employee.user().isAccountLocked(),
                    employee.user().getLastLoginAt(),
                    employee.user().getCreatedAt()
            ));
        }

        return builder.build();
    }
}
