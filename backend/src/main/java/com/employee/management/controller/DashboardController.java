package com.employee.management.controller;

import com.employee.management.dto.DashboardResponse;
import com.employee.management.dto.EmployeeDashboardResponse;
import com.employee.management.dto.ManagerDashboardResponse;
import com.employee.management.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService) {

        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardResponse>
    getAdminDashboard() {

        return ResponseEntity.ok(
                dashboardService.getAdminDashboard()
        );
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ManagerDashboardResponse>
    getManagerDashboard(
            org.springframework.security.core.Authentication authentication) {

        return ResponseEntity.ok(
                dashboardService.getManagerDashboard(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/employee")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<EmployeeDashboardResponse>
    getEmployeeDashboard(
            org.springframework.security.core.Authentication authentication) {

        return ResponseEntity.ok(
                dashboardService.getEmployeeDashboard(
                        authentication.getName()
                )
        );
    }
}