package com.employee.management.repository;

import com.employee.management.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmpCode(String empCode);

    boolean existsByEmail(String email);

    boolean existsByEmpCode(String empCode);

    List<Employee> findByDepartmentId(Long departmentId);

    List<Employee> findByManagerId(Long managerId);
}