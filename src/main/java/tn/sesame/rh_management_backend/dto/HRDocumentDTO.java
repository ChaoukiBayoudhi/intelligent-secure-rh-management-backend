package tn.sesame.rh_management_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import tn.sesame.rh_management_backend.Enumerations.DocumentAccessLevel;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HRDocumentDTO {
    private UUID id;
    private String documentName;
    private String contentType;
    private long fileSize;
    private DocumentAccessLevel level;
    private UUID employeeId;
    private String employeeName;
    private Set<String> tags;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant uploadedAt;
    private String checkSum;
}
