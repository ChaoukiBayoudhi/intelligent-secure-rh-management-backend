package tn.sesame.rh_management_backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.sesame.rh_management_backend.Entities.Employee;
import tn.sesame.rh_management_backend.Entities.HRDocument;

import java.util.List;
import java.util.UUID;

@Repository
public interface HRDocumentRepository extends JpaRepository<HRDocument, UUID> {
    List<HRDocument> findByEmployee(Employee employee);
}
