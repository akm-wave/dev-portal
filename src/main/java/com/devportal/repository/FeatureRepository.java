package com.devportal.repository;

import com.devportal.domain.entity.Feature;
import com.devportal.domain.enums.FeatureStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, UUID> {
    
    Page<Feature> findByStatus(FeatureStatus status, Pageable pageable);
    
    @Query("SELECT f FROM Feature f WHERE " +
           "LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Feature> searchByNameOrDescription(@Param("search") String search, Pageable pageable);
    
    List<Feature> findByTargetDateBefore(LocalDate date);
    
    List<Feature> findByTargetDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT f FROM Feature f JOIN f.microservices m WHERE m.id = :microserviceId")
    List<Feature> findByMicroserviceId(@Param("microserviceId") UUID microserviceId);
    
    long countByStatus(FeatureStatus status);
    
    @Query("SELECT COUNT(f) FROM Feature f JOIN f.microservices m WHERE m.id = :microserviceId")
    long countByMicroserviceId(@Param("microserviceId") UUID microserviceId);
    
    Page<Feature> findByOwnerId(UUID ownerId, Pageable pageable);
    
    @Query("SELECT f FROM Feature f WHERE f.owner.id = :userId AND f.status = :status")
    Page<Feature> findByOwnerIdAndStatus(@Param("userId") UUID userId, @Param("status") FeatureStatus status, Pageable pageable);
    
    @Query("SELECT f FROM Feature f WHERE f.owner.id = :userId AND " +
           "(LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Feature> searchByOwnerIdAndNameOrDescription(@Param("userId") UUID userId, @Param("search") String search, Pageable pageable);
}
