package com.employee.management.service;

import com.employee.management.dto.LeaveRequest;
import com.employee.management.dto.LeaveResponse;
import com.employee.management.entity.Employee;
import com.employee.management.entity.Leave;
import com.employee.management.entity.User;
import com.employee.management.enums.LeaveStatus;
import com.employee.management.exception.ResourceNotFoundException;
import com.employee.management.repository.EmployeeRepository;
import com.employee.management.repository.LeaveRepository;
import com.employee.management.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public List<LeaveResponse> getAllLeaves() {

        return leaveRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeaveResponse getLeaveById(Long id) {

        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Leave not found with id: " + id
                        )
                );

        return mapToResponse(leave);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeavesByEmployee(
            Long employeeId) {

        if (!employeeRepository.existsById(employeeId)) {

            throw new ResourceNotFoundException(
                    "Employee not found with id: "
                            + employeeId
            );
        }

        return leaveRepository
                .findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getPendingLeaves() {

        return leaveRepository
                .findByStatus(LeaveStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getManagerTeamLeaves(
            Long managerId) {

        if (!employeeRepository.existsById(managerId)) {

            throw new ResourceNotFoundException(
                    "Manager not found with id: "
                            + managerId
            );
        }

        return leaveRepository
                .findByEmployeeManagerId(managerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public LeaveResponse createLeave(
            LeaveRequest request) {

        if (request.getEndDate()
                .isBefore(request.getStartDate())) {

            throw new RuntimeException(
                    "End date cannot be before start date"
            );
        }

        Employee employee =
                employeeRepository.findById(
                        request.getEmployeeId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: "
                                        + request.getEmployeeId()
                        )
                );

        Leave leave = Leave.builder()
                .employee(employee)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        Leave savedLeave =
                leaveRepository.save(leave);

        return mapToResponse(savedLeave);
    }

    @Transactional
    public LeaveResponse updateLeave(
            Long id,
            LeaveRequest request) {

        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Leave not found with id: " + id
                        )
                );

        if (leave.getStatus() != LeaveStatus.PENDING) {

            throw new RuntimeException(
                    "Only pending leaves can be updated"
            );
        }

        if (request.getEndDate()
                .isBefore(request.getStartDate())) {

            throw new RuntimeException(
                    "End date cannot be before start date"
            );
        }

        Employee employee =
                employeeRepository.findById(
                        request.getEmployeeId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: "
                                        + request.getEmployeeId()
                        )
                );

        leave.setEmployee(employee);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());

        Leave updatedLeave =
                leaveRepository.save(leave);

        return mapToResponse(updatedLeave);
    }

    @Transactional
    public LeaveResponse approveLeave(
            Long id,
            String username) {

        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Leave not found with id: " + id
                        )
                );

        if (leave.getStatus() != LeaveStatus.PENDING) {

            throw new RuntimeException(
                    "Only pending leaves can be approved"
            );
        }

        User approver =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Approver not found"
                                )
                        );

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(approver);

        Leave approvedLeave =
                leaveRepository.save(leave);

        return mapToResponse(approvedLeave);
    }

    @Transactional
    public LeaveResponse rejectLeave(
            Long id,
            String username) {

        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Leave not found with id: " + id
                        )
                );

        if (leave.getStatus() != LeaveStatus.PENDING) {

            throw new RuntimeException(
                    "Only pending leaves can be rejected"
            );
        }

        User approver =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Approver not found"
                                )
                        );

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedBy(approver);

        Leave rejectedLeave =
                leaveRepository.save(leave);

        return mapToResponse(rejectedLeave);
    }

    @Transactional
    public void deleteLeave(Long id) {

        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Leave not found with id: " + id
                        )
                );

        if (leave.getStatus() != LeaveStatus.PENDING) {

            throw new RuntimeException(
                    "Only pending leaves can be deleted"
            );
        }

        leaveRepository.delete(leave);
    }

    private LeaveResponse mapToResponse(
            Leave leave) {

        String employeeName =
                leave.getEmployee().getFirstName()
                        + " "
                        + leave.getEmployee().getLastName();

        Long approvedById = null;
        String approvedByName = null;

        if (leave.getApprovedBy() != null) {

            approvedById =
                    leave.getApprovedBy().getId();

            User approver =
                    leave.getApprovedBy();

            if (approver.getEmployee() != null) {

                approvedByName =
                        approver.getEmployee().getFirstName()
                                + " "
                                + approver.getEmployee().getLastName();

            } else {

                approvedByName =
                        approver.getUsername();
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