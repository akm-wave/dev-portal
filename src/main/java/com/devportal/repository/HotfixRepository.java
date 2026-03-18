package com.devportal.repository;

import com.devportal.domain.entity.Hotfix;
import com.devportal.domain.enums.HotfixStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HotfixRepository extends JpaRepository<Hotfix, UUID> {

    Page<Hotfix> findByStatus(HotfixStatus status, Pageable pageable);

    List<Hotfix> findByMainFeatureId(UUID featureId);

    @Query("SELECT h FROM Hotfix h WHERE h.owner.id = :ownerId")
    List<Hotfix> findByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("SELECT COUNT(h) FROM Hotfix h WHERE h.mainFeature.id = :featureId")
    long countByMainFeatureId(@Param("featureId") UUID featureId);

    @Query("SELECT DISTINCT m FROM Hotfix h JOIN h.microservices m WHERE h.mainFeature.id = :featureId")
    List<com.devportal.domain.entity.Microservice> findDistinctMicroservicesByFeatureId(@Param("featureId") UUID featureId);

    @Query("""
            SELECT h
            FROM Hotfix h
            WHERE (:status IS NULL OR h.status = :status)
              AND (
                :search IS NULL OR :search = ''
                OR LOWER(h.title) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(h.description) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(h.releaseVersion) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<Hotfix> findAllWithFilters(
            @Param("status") com.devportal.domain.enums.HotfixStatus status,
            @Param("search") String search,
            Pageable pageable);
}
