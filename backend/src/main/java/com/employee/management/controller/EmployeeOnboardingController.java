
package com.employee.management.controller;

import com.employee.management.dto.EmployeeOnboardingRequest;
import com.employee.management.dto.EmployeeOnboardingResponse;
import com.employee.management.service.EmployeeOnboardingService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/onboarding")
public class EmployeeOnboardingController {

    private final EmployeeOnboardingService onboardingService;

    public EmployeeOnboardingController(
            EmployeeOnboardingService onboardingService) {

        this.onboardingService = onboardingService;
    }

    @PostMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeOnboardingResponse> onboardEmployee(
            @Valid @RequestBody EmployeeOnboardingRequest request) {

        EmployeeOnboardingResponse response =
                onboardingService.onboardEmployee(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
