package com.employee.management.repository;

import com.employee.management.entity.Leave;
import com.employee.management.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {

    List<Leave> findByEmployeeId(Long employeeId);

    List<Leave> findByStatus(LeaveStatus status);

    List<Leave> findByEmployeeManagerId(Long managerId);

    long countByStatus(LeaveStatus status);
}