package com.employee.management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDashboardResponse {

    private Long employeeId;
    private String employeeName;
    private String departmentName;
    private String designation;
    private long pendingLeaves;
    private long approvedLeaves;
    private long rejectedLeaves;
}