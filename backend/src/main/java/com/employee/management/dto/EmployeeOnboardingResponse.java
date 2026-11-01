
package com.employee.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EmployeeOnboardingResponse {

    private Long userId;
    private Long employeeId;
    private String username;
    private String employeeCode;
    private String fullName;
    private String email;
    private String role;
    private boolean enabled;
    private String accountStatus;
}
