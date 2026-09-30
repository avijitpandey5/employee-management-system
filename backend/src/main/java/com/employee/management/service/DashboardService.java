package com.employee.management.service;

import com.employee.management.dto.DashboardResponse;
import com.employee.management.dto.EmployeeDashboardResponse;
import com.employee.management.dto.ManagerDashboardResponse;
import com.employee.management.entity.Employee;
import com.employee.management.entity.User;
import com.employee.management.enums.LeaveStatus;
import com.employee.management.exception.ResourceNotFoundException;
import com.employee.management.repository.DepartmentRepository;
import com.employee.management.repository.EmployeeRepository;
import com.employee.management.repository.LeaveRepository;
import com.employee.management.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;

    public DashboardService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            LeaveRepository leaveRepository,
            UserRepository userRepository) {

        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.leaveRepository = leaveRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getAdminDashboard() {

        long totalEmployees =
                employeeRepository.count();

        long totalDepartments =
                departmentRepository.count();

        long pendingLeaves =
                leaveRepository.countByStatus(
                        LeaveStatus.PENDING
                );

        long approvedLeaves =
                leaveRepository.countByStatus(
                        LeaveStatus.APPROVED
                );

        long rejectedLeaves =
                leaveRepository.countByStatus(
                        LeaveStatus.REJECTED
                );

        return new DashboardResponse(
                totalEmployees,
                totalDepartments,
                pendingLeaves,
                approvedLeaves,
                rejectedLeaves
        );
    }

    @Transactional(readOnly = true)
    public ManagerDashboardResponse getManagerDashboard(
            String username) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username
                        )
                );

        if (user.getEmployee() == null) {
            throw new RuntimeException(
                    "Manager is not linked to an employee"
            );
        }

        Long managerId =
                user.getEmployee().getId();

        List<com.employee.management.entity.Leave> teamLeaves =
                leaveRepository
                        .findByEmployeeManagerId(managerId);

        long teamSize =
                employeeRepository
                        .findByManagerId(managerId)
                        .size();

        long pendingLeaves =
                teamLeaves
                        .stream()
                        .filter(leave ->
                                leave.getStatus()
                                        == LeaveStatus.PENDING
                        )
                        .count();

        long approvedLeaves =
                teamLeaves
                        .stream()
                        .filter(leave ->
                                leave.getStatus()
                                        == LeaveStatus.APPROVED
                        )
                        .count();

        long rejectedLeaves =
                teamLeaves
                        .stream()
                        .filter(leave ->
                                leave.getStatus()
                                        == LeaveStatus.REJECTED
                        )
                        .count();

        return new ManagerDashboardResponse(
                teamSize,
                pendingLeaves,
                approvedLeaves,
                rejectedLeaves
        );
    }

    @Transactional(readOnly = true)
    public EmployeeDashboardResponse getEmployeeDashboard(
            String username) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username
                        )
                );

        Employee employee = user.getEmployee();

        if (employee == null) {
            throw new RuntimeException(
                    "Employee is not linked to this user"
            );
        }

        Long employeeId = employee.getId();

        List<com.employee.management.entity.Leave> employeeLeaves =
                leaveRepository.findByEmployeeId(employeeId);

        long pendingLeaves =
                employeeLeaves
                        .stream()
                        .filter(leave ->
                                leave.getStatus()
                                        == LeaveStatus.PENDING
                        )
                        .count();

        long approvedLeaves =
                employeeLeaves
                        .stream()
                        .filter(leave ->
                                leave.getStatus()
                                        == LeaveStatus.APPROVED
                        )
                        .count();

        long rejectedLeaves =
                employeeLeaves
                        .stream()
                        .filter(leave ->
                                leave.getStatus()
                                        == LeaveStatus.REJECTED
                        )
                        .count();

        String employeeName =
                employee.getFirstName()
                        + " "
                        + employee.getLastName();

        String departmentName = null;

        if (employee.getDepartment() != null) {
            departmentName =
                    employee.getDepartment().getName();
        }

        return new EmployeeDashboardResponse(
                employeeId,
                employeeName,
                departmentName,
                employee.getDesignation(),
                pendingLeaves,
                approvedLeaves,
                rejectedLeaves
        );
    }
}