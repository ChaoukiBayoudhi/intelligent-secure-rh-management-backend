package tn.sesame.rh_management_backend.Validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import tn.sesame.rh_management_backend.Entities.Employee;
import tn.sesame.rh_management_backend.Enumerations.EmployeeJobTitle;
import java.math.BigDecimal;

public class SalaryRangeValidator implements ConstraintValidator<ValidSalaryRange, Employee> {

    @Override
    public boolean isValid(Employee employee, ConstraintValidatorContext context) {
        if (employee.jobTitle() == null || employee.salary() == null) {
            return true; // Let @NotNull handle nulls
        }

        BigDecimal salary = employee.salary();

        if (employee.jobTitle() == EmployeeJobTitle.SALES_REP) {
             // Example: Sales Rep salary must be between 3000 and 10000
             return salary.compareTo(new BigDecimal("3000")) >= 0 && 
                    salary.compareTo(new BigDecimal("10000")) <= 0;
        }
        
        if(employee.jobTitle() == EmployeeJobTitle.HR_MANAGER)
            return salary.compareTo(new BigDecimal("100000")) <= 0;

        if(employee.jobTitle() == EmployeeJobTitle.SENIOR_ENGINEER)
            return salary.compareTo(new BigDecimal("1000000")) <= 0;


        return true;
    }
}
