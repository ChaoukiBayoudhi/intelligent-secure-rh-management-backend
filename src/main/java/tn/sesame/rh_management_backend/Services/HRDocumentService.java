package tn.sesame.rh_management_backend.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.sesame.rh_management_backend.Entities.Employee;
import tn.sesame.rh_management_backend.Entities.HRDocument;
import tn.sesame.rh_management_backend.Enumerations.DocumentAccessLevel;
import tn.sesame.rh_management_backend.Repositories.EmployeeRepository;
import tn.sesame.rh_management_backend.Repositories.HRDocumentRepository;
import tn.sesame.rh_management_backend.dto.HRDocumentDTO;
import tn.sesame.rh_management_backend.dto.HRDocumentUploadRequest;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HRDocumentService {

    private final HRDocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    
    // In production, use a proper key management system
    private static final String ENCRYPTION_KEY = "MySecretKey12345"; // 16 chars for AES-128

    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    @Transactional
    public HRDocumentDTO uploadDocument(HRDocumentUploadRequest request) throws Exception {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("Employee not found"));

        // Encrypt content
        byte[] encryptedContent = encryptContent(request.getContent());

        // Calculate checksum
        String checksum = calculateChecksum(request.getContent());

        HRDocument document = HRDocument.builder()
                .documentName(request.getDocumentName())
                .contentType(request.getContentType())
                .encrypedContent(encryptedContent)
                .fileSize(request.getContent().length)
                .level(request.getLevel())
                .employee(employee)
                .tags(request.getTags())
                .uploadedAt(Instant.now())
                .checkSum(checksum)
                .build();

        document = documentRepository.save(document);
        return convertToDTO(document);
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR_MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public List<HRDocumentDTO> getAllDocuments() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<HRDocument> documents;

        if (hasRole(auth, "ROLE_ADMIN") || hasRole(auth, "ROLE_HR_MANAGER")) {
            documents = documentRepository.findAll();
        } else {
            // Filter documents based on user's access level
            documents = documentRepository.findAll().stream()
                    .filter(doc -> canAccessDocument(doc, auth))
                    .collect(Collectors.toList());
        }

        return documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR_MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public HRDocumentDTO getDocumentById(UUID id) {
        HRDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("Document not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!canAccessDocument(document, auth)) {
            throw new tn.sesame.rh_management_backend.exceptions.ForbiddenException("Access denied");
        }

        return convertToDTO(document);
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR_MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public byte[] downloadDocument(UUID id) throws Exception {
        HRDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("Document not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!canAccessDocument(document, auth)) {
            throw new tn.sesame.rh_management_backend.exceptions.ForbiddenException("Access denied");
        }

        // Decrypt and return content
        return decryptContent(document.encrypedContent());
    }

    /**
     * Retrieves a list of document DTOs associated with a specific employee.
     *
     * @param employeeId the unique identifier of the employee whose documents are to be fetched
     * @return a list of {@code HRDocumentDTO} objects representing all documents linked to the specified employee
     * @throws tn.sesame.rh_management_backend.exceptions.NotFoundException if no employee with the given ID is found
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'HR_MANAGER', 'ADMIN')")
    //@Transctional annotation is used to tell spring to open a transaction for this method
    //readOnly = true → optimized for read operations, not writes.
    //by default, readOnly = false which means for writing operations.
    @Transactional(readOnly = true)
    public List<HRDocumentDTO> getDocumentsByEmployee(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("Employee not found"));

        return documentRepository.findByEmployee(employee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteDocument(UUID id) {
        HRDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new tn.sesame.rh_management_backend.exceptions.NotFoundException("Document not found"));
        documentRepository.delete(document);
    }

    private boolean canAccessDocument(HRDocument document, Authentication auth) {
        String userEmail = auth.getName();

        // Admin and HR Manager can access all documents
        if (hasRole(auth, "ROLE_ADMIN") || hasRole(auth, "ROLE_HR_MANAGER")) {
            return true;
        }

        // Check if user is the document owner
        if (document.employee() != null && 
            document.employee().user() != null && 
            document.employee().user().getEmail().equals(userEmail)) {
            return true;
        }

        // Managers can access their subordinates' documents
        if (hasRole(auth, "ROLE_MANAGER") && document.level() != DocumentAccessLevel.CONFIDENTIAL) {
            return true;
        }

        return false;
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private byte[] encryptContent(byte[] content) throws Exception {
        SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(content);
    }

    private byte[] decryptContent(byte[] encryptedContent) throws Exception {
        SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedContent);
    }

    private String calculateChecksum(byte[] content) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(content);
        return Base64.getEncoder().encodeToString(hash);
    }

    private HRDocumentDTO convertToDTO(HRDocument document) {
        HRDocumentDTO.HRDocumentDTOBuilder builder = HRDocumentDTO.builder()
                .id(document.id())
                .documentName(document.documentName())
                .contentType(document.contentType())
                .fileSize(document.fileSize())
                .level(document.level())
                .tags(document.tags())
                .uploadedAt(document.uploadedAt())
                .checkSum(document.checkSum());

        if (document.employee() != null) {
            builder.employeeId(document.employee().id())
                   .employeeName(document.employee().firstName() + " " + document.employee().lastName());
        }

        return builder.build();
    }
}
