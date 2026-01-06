package tn.sesame.rh_management_backend.Web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.sesame.rh_management_backend.Services.HRDocumentService;
import tn.sesame.rh_management_backend.dto.HRDocumentDTO;
import tn.sesame.rh_management_backend.dto.HRDocumentUploadRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class HRDocumentController {

    private final HRDocumentService documentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<HRDocumentDTO> uploadDocument(@Valid @RequestBody HRDocumentUploadRequest request) {
        try {
            HRDocumentDTO document = documentService.uploadDocument(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(document);
        } catch (Exception e) {
            throw new RuntimeException("Document upload failed: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<List<HRDocumentDTO>> getAllDocuments() {
        List<HRDocumentDTO> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<HRDocumentDTO> getDocumentById(@PathVariable UUID id) {
        HRDocumentDTO document = documentService.getDocumentById(id);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable UUID id) {
        try {
            HRDocumentDTO document = documentService.getDocumentById(id);
            byte[] content = documentService.downloadDocument(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", document.getDocumentName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);
        } catch (Exception e) {
            throw new RuntimeException("Document download failed: " + e.getMessage());
        }
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<List<HRDocumentDTO>> getDocumentsByEmployee(@PathVariable UUID employeeId) {
        List<HRDocumentDTO> documents = documentService.getDocumentsByEmployee(employeeId);
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteDocument(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok("Document deleted successfully");
    }
}
