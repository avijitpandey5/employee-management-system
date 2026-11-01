
package com.employee.management.service;

import com.employee.management.dto.DocumentResponse;
import com.employee.management.entity.Document;
import com.employee.management.entity.Employee;
import com.employee.management.entity.User;
import com.employee.management.enums.Role;
import com.employee.management.exception.ResourceNotFoundException;
import com.employee.management.repository.DocumentRepository;
import com.employee.management.repository.EmployeeRepository;
import com.employee.management.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final Path uploadDirectory;

    public DocumentService(
            DocumentRepository documentRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            @Value("${file.upload-dir:uploads/documents}") String uploadDirectory) {

        this.documentRepository = documentRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;

        this.uploadDirectory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }

    @Transactional
    public DocumentResponse uploadDocument(
            Long employeeId,
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a file to upload"
            );
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "File size must not exceed 10 MB"
            );
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + employeeId
                        )
                );

        // Check whether the authenticated user may upload for this employee.
        authorizeEmployeeAccess(employee);

        String originalFileName = StringUtils.cleanPath(
                file.getOriginalFilename() == null
                        ? "document"
                        : file.getOriginalFilename()
        );

        if (originalFileName.isBlank()
                || originalFileName.contains("..")
                || originalFileName.contains("/")
                || originalFileName.contains("\\")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file name"
            );
        }

        String storedFileName =
                UUID.randomUUID() + "_" + originalFileName;

        Path destination = uploadDirectory
                .resolve(storedFileName)
                .normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file path"
            );
        }

        try {
            Files.createDirectories(uploadDirectory);

            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to save uploaded file"
            );
        }

        Document document = Document.builder()
                .employee(employee)
                .fileName(originalFileName)
                .fileType(
                        file.getContentType() == null
                                ? "application/octet-stream"
                                : file.getContentType()
                )
                .filePath(storedFileName)
                .build();

        try {
            Document savedDocument = documentRepository.save(document);
            return mapToResponse(savedDocument);
        } catch (RuntimeException exception) {
            try {
                Files.deleteIfExists(destination);
            } catch (IOException ignored) {
                // Preserve the original database exception.
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments() {

        requireAdmin();

        return documentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByEmployee(
            Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + employeeId
                        )
                );

        authorizeEmployeeAccess(employee);

        return documentRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource downloadDocument(Long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document not found with id: " + documentId
                        )
                );

        // Check permissions against the employee who owns this document.
        authorizeEmployeeAccess(document.getEmployee());

        Path filePath = uploadDirectory
                .resolve(document.getFilePath())
                .normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid stored file path"
            );
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException(
                        "Stored document file was not found"
                );
            }

            return resource;
        } catch (MalformedURLException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to read document"
            );
        }
    }

    @Transactional
    public void deleteDocument(Long documentId) {

        // Deletion is restricted to administrators.
        requireAdmin();

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document not found with id: " + documentId
                        )
                );

        Path filePath = uploadDirectory
                .resolve(document.getFilePath())
                .normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid stored file path"
            );
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to delete stored document"
            );
        }

        documentRepository.delete(document);
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication required");
        }

        return userRepository.findByUsername(authentication.getName())
                .filter(User::isEnabled)
                .orElseThrow(() ->
                        new AccessDeniedException("User access denied")
                );
    }

    private void requireAdmin() {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException(
                    "Only administrators can perform this operation"
            );
        }
    }

    private void authorizeEmployeeAccess(Employee targetEmployee) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        Employee currentEmployee = currentUser.getEmployee();

        if (currentEmployee == null) {
            throw new AccessDeniedException(
                    "Your user account is not linked to an employee"
            );
        }

        if (currentUser.getRole() == Role.EMPLOYEE) {
            if (currentEmployee.getId().equals(targetEmployee.getId())) {
                return;
            }

            throw new AccessDeniedException(
                    "You can access only your own documents"
            );
        }

        if (currentUser.getRole() == Role.MANAGER) {
            Employee manager = targetEmployee.getManager();

            if (manager != null
                    && currentEmployee.getId().equals(manager.getId())) {
                return;
            }

            throw new AccessDeniedException(
                    "You can access documents only for your direct reports"
            );
        }

        throw new AccessDeniedException("Document access denied");
    }

    private DocumentResponse mapToResponse(Document document) {

        Employee employee = document.getEmployee();

        String employeeName =
                employee.getFirstName() + " " + employee.getLastName();

        return new DocumentResponse(
                document.getId(),
                employee.getId(),
                employeeName,
                document.getFileName(),
                document.getFileType(),
                document.getUploadedAt()
        );
    }
}
