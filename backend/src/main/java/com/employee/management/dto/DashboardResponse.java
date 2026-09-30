package com.employee.management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long totalEmployees;
    private long totalDepartments;
    private long pendingLeaves;
    private long approvedLeaves;
    private long rejectedLeaves;
}