
package com.employee.management.service;

import com.employee.management.dto.EmployeeRequest;
import com.employee.management.dto.EmployeeResponse;
import com.employee.management.entity.Department;
import com.employee.management.entity.Employee;
import com.employee.management.entity.User;
import com.employee.management.exception.ResourceNotFoundException;
import com.employee.management.repository.DepartmentRepository;
import com.employee.management.repository.EmployeeRepository;
import com.employee.management.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository) {

        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // GET ALL EMPLOYEES - ADMIN
    // ==========================================

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {

        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ==========================================
    // GET EMPLOYEE BY ID - ADMIN
    // ==========================================

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + id
                        )
                );

        return mapToResponse(employee);
    }

    // ==========================================
    // GET MY PROFILE - AUTHENTICATED USER
    // ==========================================

    @Transactional(readOnly = true)
    public EmployeeResponse getMyProfile() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user not found"
                        )
                );

        if (user.getEmployee() == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No employee profile is linked to this account"
            );
        }

        Long employeeId = user.getEmployee().getId();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee profile not found"
                        )
                );

        return mapToResponse(employee);
    }

    // ==========================================
    // CREATE EMPLOYEE - ADMIN
    // ==========================================

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {

        if (employeeRepository.existsByEmpCode(
                request.getEmpCode())) {

            throw new RuntimeException(
                    "Employee code already exists: "
                            + request.getEmpCode()
            );
        }

        if (employeeRepository.existsByEmail(
                request.getEmail())) {

            throw new RuntimeException(
                    "Email already exists: "
                            + request.getEmail()
            );
        }

        Department department = null;

        if (request.getDepartmentId() != null) {

            department = departmentRepository
                    .findById(request.getDepartmentId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Department not found with id: "
                                            + request.getDepartmentId()
                            )
                    );
        }

        Employee manager = null;

        if (request.getManagerId() != null) {

            manager = employeeRepository
                    .findById(request.getManagerId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Manager not found with id: "
                                            + request.getManagerId()
                            )
                    );
        }

        Employee employee = Employee.builder()
                .empCode(request.getEmpCode())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .salary(request.getSalary())
                .designation(request.getDesignation())
                .hireDate(request.getHireDate())
                .department(department)
                .manager(manager)
                .build();

        Employee savedEmployee =
                employeeRepository.save(employee);

        return mapToResponse(savedEmployee);
    }

    // ==========================================
    // UPDATE EMPLOYEE - ADMIN
    // ==========================================

    @Transactional
    public EmployeeResponse updateEmployee(
            Long id,
            EmployeeRequest request) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + id
                        )
                );

        if (!employee.getEmpCode().equals(request.getEmpCode())
                && employeeRepository.existsByEmpCode(
                        request.getEmpCode())) {

            throw new RuntimeException(
                    "Employee code already exists: "
                            + request.getEmpCode()
            );
        }

        if (!employee.getEmail().equals(request.getEmail())
                && employeeRepository.existsByEmail(
                        request.getEmail())) {

            throw new RuntimeException(
                    "Email already exists: "
                            + request.getEmail()
            );
        }

        Department department = null;

        if (request.getDepartmentId() != null) {

            department = departmentRepository
                    .findById(request.getDepartmentId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Department not found with id: "
                                            + request.getDepartmentId()
                            )
                    );
        }

        Employee manager = null;

        if (request.getManagerId() != null) {

            if (request.getManagerId().equals(id)) {

                throw new RuntimeException(
                        "Employee cannot be their own manager"
                );
            }

            manager = employeeRepository
                    .findById(request.getManagerId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Manager not found with id: "
                                            + request.getManagerId()
                            )
                    );
        }

        employee.setEmpCode(request.getEmpCode());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setSalary(request.getSalary());
        employee.setDesignation(request.getDesignation());
        employee.setHireDate(request.getHireDate());
        employee.setDepartment(department);
        employee.setManager(manager);

        Employee updatedEmployee =
                employeeRepository.save(employee);

        return mapToResponse(updatedEmployee);
    }

    // ==========================================
    // DELETE EMPLOYEE - ADMIN
    // ==========================================

    @Transactional
    public void deleteEmployee(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + id
                        )
                );

        employeeRepository.delete(employee);
    }

    // ==========================================
    // ENTITY TO RESPONSE MAPPING
    // ==========================================

    private EmployeeResponse mapToResponse(Employee employee) {

        EmployeeResponse response = new EmployeeResponse();

        response.setId(employee.getId());
        response.setEmpCode(employee.getEmpCode());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setPhone(employee.getPhone());
        response.setSalary(employee.getSalary());
        response.setDesignation(employee.getDesignation());
        response.setHireDate(employee.getHireDate());

        if (employee.getDepartment() != null) {

            response.setDepartmentId(
                    employee.getDepartment().getId()
            );

            response.setDepartmentName(
                    employee.getDepartment().getName()
            );
        }

        if (employee.getManager() != null) {

            response.setManagerId(
                    employee.getManager().getId()
            );

            response.setManagerName(
                    employee.getManager().getFirstName()
                            + " "
                            + employee.getManager().getLastName()
            );
        }

        return response;
    }
}
