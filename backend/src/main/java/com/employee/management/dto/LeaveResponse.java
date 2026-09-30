package com.employee.management.dto;

import com.employee.management.enums.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponse {

    private Long id;

    private Long employeeId;

    private String employeeName;

    private LocalDate startDate;

    private LocalDate endDate;

    private String reason;

    private LeaveStatus status;

    private Long approvedById;

    private String approvedByName;
}