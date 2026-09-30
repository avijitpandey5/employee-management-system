package com.employee.management.controller;

import com.employee.management.dto.LeaveRequest;
import com.employee.management.dto.LeaveResponse;
import com.employee.management.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // =========================
    // GET ALL LEAVES
    // =========================
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveResponse>> getAllLeaves() {

        return ResponseEntity.ok(
                leaveService.getAllLeaves()
        );
    }

    // =========================
    // GET LEAVE BY ID
    // =========================
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveResponse> getLeaveById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                leaveService.getLeaveById(id)
        );
    }

    // =========================
    // GET LEAVES BY EMPLOYEE
    // =========================
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<LeaveResponse>> getLeavesByEmployee(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                leaveService.getLeavesByEmployee(employeeId)
        );
    }

    // =========================
    // GET PENDING LEAVES
    // =========================
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<LeaveResponse>> getPendingLeaves() {

        return ResponseEntity.ok(
                leaveService.getPendingLeaves()
        );
    }

    // =========================
    // GET MANAGER TEAM LEAVES
    // =========================
    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<LeaveResponse>> getManagerTeamLeaves(
            @PathVariable Long managerId) {

        return ResponseEntity.ok(
                leaveService.getManagerTeamLeaves(managerId)
        );
    }

    // =========================
    // APPLY FOR LEAVE
    // =========================
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<LeaveResponse> createLeave(
            @Valid @RequestBody LeaveRequest request) {

        return ResponseEntity.ok(
                leaveService.createLeave(request)
        );
    }

    // =========================
    // UPDATE LEAVE
    // =========================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<LeaveResponse> updateLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequest request) {

        return ResponseEntity.ok(
                leaveService.updateLeave(id, request)
        );
    }

    // =========================
    // APPROVE LEAVE
    // =========================
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<LeaveResponse> approveLeave(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication) {

        return ResponseEntity.ok(
                leaveService.approveLeave(
                        id,
                        authentication.getName()
                )
        );
    }

    // =========================
    // REJECT LEAVE
    // =========================
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<LeaveResponse> rejectLeave(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication) {

        return ResponseEntity.ok(
                leaveService.rejectLeave(
                        id,
                        authentication.getName()
                )
        );
    }

    // =========================
    // DELETE LEAVE
    // =========================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> deleteLeave(
            @PathVariable Long id) {

        leaveService.deleteLeave(id);

        return ResponseEntity.noContent().build();
    }
}