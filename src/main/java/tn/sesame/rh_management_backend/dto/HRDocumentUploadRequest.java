package tn.sesame.rh_management_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.sesame.rh_management_backend.Enumerations.DocumentAccessLevel;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HRDocumentUploadRequest {
    @NotBlank(message = "Document name is required")
    private String documentName;

    @NotNull(message = "Document content is required")
    private byte[] content;

    private String contentType;

    @NotNull(message = "Access level is required")
    private DocumentAccessLevel level;

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    private Set<String> tags;
}
