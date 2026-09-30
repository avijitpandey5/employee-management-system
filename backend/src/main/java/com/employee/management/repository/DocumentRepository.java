package com.employee.management.repository;

import com.employee.management.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByEmployeeId(Long employeeId);
}