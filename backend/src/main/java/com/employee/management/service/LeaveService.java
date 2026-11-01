
package com.employee.management.service;

import com.employee.management.dto.LeaveRequest;
import com.employee.management.dto.LeaveResponse;
import com.employee.management.entity.Employee;
import com.employee.management.entity.Leave;
import com.employee.management.entity.User;
import com.employee.management.enums.LeaveStatus;
import com.employee.management.enums.Role;
import com.employee.management.exception.ResourceNotFoundException;
import com.employee.management.repository.EmployeeRepository;
import com.employee.management.repository.LeaveRepository;
import com.employee.management.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public LeaveService(
            LeaveRepository leaveRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository) {
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Current user not found"));
    }

    private boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    private boolean isOwnEmployee(User user, Employee employee) {
        return user.getEmployee() != null
                && Objects.equals(
                        user.getEmployee().getId(),
                        employee.getId());
    }

    private boolean isDirectReport(User manager, Employee employee) {
        return manager.getEmployee() != null
                && employee.getManager() != null
                && Objects.equals(
                        employee.getManager().getId(),
                        manager.getEmployee().getId());
    }

    private void denyAccess(String message) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, message);
    }

    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + employeeId));
    }

    private Leave findLeave(Long id) {
        return leaveRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Leave not found with id: " + id));
    }

    private void validateDates(LeaveRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date cannot be before start date");
        }
    }

    private void requirePending(Leave leave, String action) {
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only pending leaves can be " + action);
        }
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getAllLeaves() {
        return leaveRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeaveResponse getLeaveById(Long id) {
        return mapToResponse(findLeave(id));
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeavesByEmployee(Long employeeId) {
        User currentUser = getCurrentUser();
        Employee target = findEmployee(employeeId);

        boolean allowed = isAdmin(currentUser)
                || isOwnEmployee(currentUser, target)
                || (currentUser.getRole() == Role.MANAGER
                    && isDirectReport(currentUser, target));

        if (!allowed) {
            denyAccess("You cannot view this employee's leaves");
        }

        return leaveRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getPendingLeaves() {
        User currentUser = getCurrentUser();

        if (isAdmin(currentUser)) {
            return leaveRepository.findByStatus(LeaveStatus.PENDING)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        if (currentUser.getRole() != Role.MANAGER
                || currentUser.getEmployee() == null) {
            denyAccess("Only admins and managers can view pending team leaves");
        }

        return leaveRepository
                .findByEmployeeManagerId(currentUser.getEmployee().getId())
                .stream()
                .filter(leave -> leave.getStatus() == LeaveStatus.PENDING)
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getManagerTeamLeaves(Long managerId) {
        User currentUser = getCurrentUser();

        if (!isAdmin(currentUser)) {
            if (currentUser.getRole() != Role.MANAGER
                    || currentUser.getEmployee() == null
                    || !Objects.equals(
                            currentUser.getEmployee().getId(), managerId)) {
                denyAccess("You can only view your own team's leaves");
            }
        }

        if (!employeeRepository.existsById(managerId)) {
            throw new ResourceNotFoundException(
                    "Manager not found with id: " + managerId);
        }

        return leaveRepository.findByEmployeeManagerId(managerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public LeaveResponse createLeave(LeaveRequest request) {
        User currentUser = getCurrentUser();
        validateDates(request);

        Employee employee = findEmployee(request.getEmployeeId());

        if (!isAdmin(currentUser) && !isOwnEmployee(currentUser, employee)) {
            denyAccess("You can only apply for leave for yourself");
        }

        Leave leave = Leave.builder()
                .employee(employee)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        return mapToResponse(leaveRepository.save(leave));
    }

    @Transactional
    public LeaveResponse updateLeave(Long id, LeaveRequest request) {
        User currentUser = getCurrentUser();
        Leave leave = findLeave(id);

        requirePending(leave, "updated");
        validateDates(request);

        if (!isAdmin(currentUser)
                && !isOwnEmployee(currentUser, leave.getEmployee())) {
            denyAccess("You can only update your own leave requests");
        }

        Employee requestedEmployee = findEmployee(request.getEmployeeId());

        // Non-admin users cannot transfer a leave request to another employee.
        if (!isAdmin(currentUser)
                && !Objects.equals(
                        leave.getEmployee().getId(),
                        requestedEmployee.getId())) {
            denyAccess("You cannot change the employee on a leave request");
        }

        leave.setEmployee(requestedEmployee);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());

        return mapToResponse(leaveRepository.save(leave));
    }

    @Transactional
    public LeaveResponse approveLeave(Long id, String username) {
        return changeLeaveStatus(id, username, LeaveStatus.APPROVED);
    }

    @Transactional
    public LeaveResponse rejectLeave(Long id, String username) {
        return changeLeaveStatus(id, username, LeaveStatus.REJECTED);
    }

    private LeaveResponse changeLeaveStatus(
            Long id, String username, LeaveStatus newStatus) {

        User approver = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Approver not found"));

        Leave leave = findLeave(id);
        requirePending(
                leave,
                newStatus == LeaveStatus.APPROVED ? "approved" : "rejected");

        if (!isAdmin(approver)) {
            if (approver.getRole() != Role.MANAGER
                    || !isDirectReport(approver, leave.getEmployee())) {
                denyAccess("Managers can only review leaves of their direct reports");
            }
        }

        leave.setStatus(newStatus);
        leave.setApprovedBy(approver);

        return mapToResponse(leaveRepository.save(leave));
    }

    @Transactional
    public void deleteLeave(Long id) {
        User currentUser = getCurrentUser();
        Leave leave = findLeave(id);

        requirePending(leave, "deleted");

        if (!isAdmin(currentUser)
                && !isOwnEmployee(currentUser, leave.getEmployee())) {
            denyAccess("You can only delete your own leave requests");
        }

        leaveRepository.delete(leave);
    }

    private LeaveResponse mapToResponse(Leave leave) {
        String employeeName =
                leave.getEmployee().getFirstName()
                        + " "
                        + leave.getEmployee().getLastName();

        Long approvedById = null;
        String approvedByName = null;

        if (leave.getApprovedBy() != null) {
            User approver = leave.getApprovedBy();
            approvedById = approver.getId();

            if (approver.getEmployee() != null) {
                approvedByName =
                        approver.getEmployee().getFirstName()
                                + " "
                                + approver.getEmployee().getLastName();
            } else {
                approvedByName = approver.getUsername();
            }
        }

        return new LeaveResponse(
                leave.getId(),
                leave.getEmployee().getId(),
                employeeName,
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                leave.getStatus(),
                approvedById,
                approvedByName
        );
    }
}
