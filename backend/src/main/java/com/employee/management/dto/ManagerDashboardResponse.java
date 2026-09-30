package com.employee.management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDashboardResponse {

    private long teamSize;
    private long pendingLeaves;
    private long approvedLeaves;
    private long rejectedLeaves;
}