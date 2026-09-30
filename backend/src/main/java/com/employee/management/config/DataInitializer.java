package com.employee.management.config;

import com.employee.management.entity.Department;
import com.employee.management.entity.Employee;
import com.employee.management.entity.User;
import com.employee.management.enums.Role;
import com.employee.management.repository.DepartmentRepository;
import com.employee.management.repository.EmployeeRepository;
import com.employee.management.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (userRepository.existsByUsername("admin")) {
                return;
            }

            // =========================
            // DEPARTMENT
            // =========================

            Department itDepartment =
                    Department.builder()
                            .name("Information Technology")
                            .location("Pune")
                            .build();

            itDepartment =
                    departmentRepository.save(itDepartment);


            // =========================
            // MANAGER
            // =========================

            Employee manager =
                    Employee.builder()
                            .empCode("EMP001")
                            .firstName("Rahul")
                            .lastName("Sharma")
                            .email("rahul.manager@company.com")
                            .phone("9876543210")
                            .salary(new BigDecimal("90000"))
                            .designation("Engineering Manager")
                            .hireDate(LocalDate.now())
                            .department(itDepartment)
                            .build();

            manager =
                    employeeRepository.save(manager);


            // =========================
            // EMPLOYEE
            // =========================

            Employee employee =
                    Employee.builder()
                            .empCode("EMP002")
                            .firstName("Amit")
                            .lastName("Kumar")
                            .email("amit.employee@company.com")
                            .phone("9876543211")
                            .salary(new BigDecimal("60000"))
                            .designation("Software Engineer")
                            .hireDate(LocalDate.now())
                            .department(itDepartment)
                            .manager(manager)
                            .build();

            employee =
                    employeeRepository.save(employee);


            // =========================
            // ADMIN USER
            // =========================

            User admin =
                    User.builder()
                            .username("admin")
                            .password(
                                    passwordEncoder.encode("admin123")
                            )
                            .role(Role.ADMIN)
                            .build();

            userRepository.save(admin);


            // =========================
            // MANAGER USER
            // =========================

            User managerUser =
                    User.builder()
                            .username("manager")
                            .password(
                                    passwordEncoder.encode("manager123")
                            )
                            .role(Role.MANAGER)
                            .employee(manager)
                            .build();

            userRepository.save(managerUser);


            // =========================
            // EMPLOYEE USER
            // =========================

            User employeeUser =
                    User.builder()
                            .username("employee")
                            .password(
                                    passwordEncoder.encode("employee123")
                            )
                            .role(Role.EMPLOYEE)
                            .employee(employee)
                            .build();

            userRepository.save(employeeUser);

            System.out.println(
                    "========================================"
            );
            System.out.println(
                    "Initial users created successfully!"
            );
            System.out.println(
                    "Admin    : admin / admin123"
            );
            System.out.println(
                    "Manager  : manager / manager123"
            );
            System.out.println(
                    "Employee : employee / employee123"
            );
            System.out.println(
                    "========================================"
            );
        };
    }
}