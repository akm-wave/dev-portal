package com.devportal.repository;

import com.devportal.domain.entity.Incident;
import com.devportal.domain.enums.IncidentStatus;
import com.devportal.domain.enums.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);

    Page<Incident> findBySeverity(Severity severity, Pageable pageable);

    List<Incident> findByMainFeatureId(UUID featureId);

    @Query("SELECT i FROM Incident i WHERE i.owner.id = :ownerId")
    List<Incident> findByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.mainFeature.id = :featureId")
    long countByMainFeatureId(@Param("featureId") UUID featureId);

    @Query("SELECT i.severity, COUNT(i) FROM Incident i WHERE i.mainFeature.id = :featureId GROUP BY i.severity")
    List<Object[]> countByFeatureGroupedBySeverity(@Param("featureId") UUID featureId);

    @Query("SELECT DISTINCT m FROM Incident i JOIN i.microservices m WHERE i.mainFeature.id = :featureId")
    List<com.devportal.domain.entity.Microservice> findDistinctMicroservicesByFeatureId(@Param("featureId") UUID featureId);

    @Query("""
            SELECT i
            FROM Incident i
            WHERE (:status IS NULL OR i.status = :status)
              AND (
                :search IS NULL OR :search = ''
                OR LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<Incident> findAllWithFilters(
            @Param("status") IncidentStatus status,
            @Param("search") String search,
            Pageable pageable);
}
