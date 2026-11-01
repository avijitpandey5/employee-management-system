package com.employee.management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private Long id;

    private Long employeeId;

    private String employeeName;

    private String fileName;

    private String fileType;

    private LocalDateTime uploadedAt;
}