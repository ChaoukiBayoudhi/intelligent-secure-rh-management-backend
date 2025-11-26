package tn.sesame.rh_management_backend.Entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import tn.sesame.rh_management_backend.Enumerations.DocumentAccessLevel;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Accessors(fluent = true)//calling the getters and setters without using the prefixes get and set
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(staticName = "of")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Table(name = "hr_documents")
public class HRDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @Column(unique = true)
    @NonNull
    String documentName;
    String contentType;
    @NonNull
    @Lob
    byte[] encrypedContent;
    long fileSize;
    @Enumerated(EnumType.STRING)
    DocumentAccessLevel level;

    @ManyToOne(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    @JoinColumn(name = "owner_id")
    Employee employee;

    @ElementCollection
    @CollectionTable(name = "document_tags", joinColumns = @JoinColumn(name = "document_id"))

    Set<String> tags=new HashSet<>();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    Instant uploadedAt;
    String checkSum;

}
