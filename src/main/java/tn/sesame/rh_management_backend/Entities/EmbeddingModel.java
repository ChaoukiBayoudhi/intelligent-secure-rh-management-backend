package tn.sesame.rh_management_backend.Entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;
import java.util.Vector;
@Builder
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
@Table(name = "embedding_models")
public class EmbeddingModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    HRDocument document;
    @NonNull
    String content;
    Vector<Object> embedding=new Vector<>(1536);
    String metaData;
    Instant createdAt;

}
