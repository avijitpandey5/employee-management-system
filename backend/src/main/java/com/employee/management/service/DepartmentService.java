package com.employee.management.service;

import com.employee.management.dto.DepartmentRequest;
import com.employee.management.dto.DepartmentResponse;
import com.employee.management.entity.Department;
import com.employee.management.exception.ResourceNotFoundException;
import com.employee.management.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(
            DepartmentRepository departmentRepository) {

        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {

        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {

        Department department =
                departmentRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Department not found with id: "
                                                + id
                                )
                        );

        return mapToResponse(department);
    }

    @Transactional
    public DepartmentResponse createDepartment(
            DepartmentRequest request) {

        if (departmentRepository.existsByName(
                request.getName())) {

            throw new RuntimeException(
                    "Department already exists: "
                            + request.getName()
            );
        }

        Department department = Department.builder()
                .name(request.getName())
                .location(request.getLocation())
                .build();

        Department savedDepartment =
                departmentRepository.save(department);

        return mapToResponse(savedDepartment);
    }

    @Transactional
    public DepartmentResponse updateDepartment(
            Long id,
            DepartmentRequest request) {

        Department department =
                departmentRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Department not found with id: "
                                                + id
                                )
                        );

        if (!department.getName().equals(
                request.getName())
                && departmentRepository.existsByName(
                request.getName())) {

            throw new RuntimeException(
                    "Department already exists: "
                            + request.getName()
            );
        }

        department.setName(request.getName());
        department.setLocation(request.getLocation());

        Department updatedDepartment =
                departmentRepository.save(department);

        return mapToResponse(updatedDepartment);
    }

    @Transactional
    public void deleteDepartment(Long id) {

        Department department =
                departmentRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Department not found with id: "
                                                + id
                                )
                        );

        if (!department.getEmployees().isEmpty()) {

            throw new RuntimeException(
                    "Cannot delete department because "
                            + "employees are assigned to it"
            );
        }

        departmentRepository.delete(department);
    }

    private DepartmentResponse mapToResponse(
            Department department) {

        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getLocation(),
                department.getEmployees().size()
        );
    }
}