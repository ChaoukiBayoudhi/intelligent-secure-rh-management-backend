package tn.sesame.rh_management_backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.sesame.rh_management_backend.Entities.EmbeddingModel;

import java.util.UUID;

@Repository
public interface EmbeddingModelRepository extends JpaRepository<EmbeddingModel, UUID> {
}
