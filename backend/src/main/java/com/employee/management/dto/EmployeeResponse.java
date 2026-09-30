package com.employee.management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;

    private String empCode;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private BigDecimal salary;

    private String designation;

    private LocalDate hireDate;

    private Long departmentId;

    private String departmentName;

    private Long managerId;

    private String managerName;
}