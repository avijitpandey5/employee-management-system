
package com.employee.management.service;

import com.employee.management.dto.EmployeeOnboardingRequest;
import com.employee.management.dto.EmployeeOnboardingResponse;
import com.employee.management.dto.EmployeeRequest;
import com.employee.management.entity.Department;
import com.employee.management.entity.Employee;
import com.employee.management.entity.User;
import com.employee.management.enums.Role;
import com.employee.management.repository.DepartmentRepository;
import com.employee.management.repository.EmployeeRepository;
import com.employee.management.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EmployeeOnboardingService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public EmployeeOnboardingService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public EmployeeOnboardingResponse onboardEmployee(
            EmployeeOnboardingRequest request) {

        String username = request.getUsername().trim();
        EmployeeRequest details = request.getEmployee();

        // 1. Check username
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        // 2. Check employee code
        if (employeeRepository.existsByEmpCode(
                details.getEmpCode())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee code already exists"
            );
        }

        // 3. Check email
        if (employeeRepository.existsByEmail(
                details.getEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee email already exists"
            );
        }

        // 4. Find department
        Department department = departmentRepository
                .findById(details.getDepartmentId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Department not found"
                        )
                );

        // 5. Find manager, if provided
        Employee manager = null;

        if (details.getManagerId() != null) {

            manager = employeeRepository
                    .findById(details.getManagerId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Manager not found"
                            )
                    );
        }

        // 6. Create employee
        Employee employee = Employee.builder()
                .empCode(details.getEmpCode())
                .firstName(details.getFirstName())
                .lastName(details.getLastName())
                .email(details.getEmail())
                .phone(details.getPhone())
                .salary(details.getSalary())
                .designation(details.getDesignation())
                .hireDate(details.getHireDate())
                .department(department)
                .manager(manager)
                .build();

        Employee savedEmployee =
                employeeRepository.save(employee);

        // 7. Generate an unknown, random password.
        // This is NOT an activation credential.
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String unusablePassword = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // 8. Create a disabled account
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(unusablePassword))
                .role(Role.EMPLOYEE)
                .employee(savedEmployee)
                .enabled(false)
                .build();

        User savedUser = userRepository.save(user);

        // 9. Return onboarding result
        return EmployeeOnboardingResponse.builder()
                .userId(savedUser.getId())
                .employeeId(savedEmployee.getId())
                .username(savedUser.getUsername())
                .employeeCode(savedEmployee.getEmpCode())
                .fullName(
                        savedEmployee.getFirstName() + " "
                        + savedEmployee.getLastName()
                )
                .email(savedEmployee.getEmail())
                .role(savedUser.getRole().name())
                .enabled(savedUser.isEnabled())
                .accountStatus("PENDING_ACTIVATION")
                .build();
    }
}
