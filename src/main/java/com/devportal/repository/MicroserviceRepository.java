package com.devportal.repository;

import com.devportal.domain.entity.Microservice;
import com.devportal.domain.enums.MicroserviceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MicroserviceRepository extends JpaRepository<Microservice, UUID> {
    
    Page<Microservice> findByStatus(MicroserviceStatus status, Pageable pageable);
    
    @Query("SELECT m FROM Microservice m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Microservice> searchByNameOrDescription(@Param("search") String search, Pageable pageable);
    
    List<Microservice> findByIdIn(List<UUID> ids);
    
    @Query("SELECT m FROM Microservice m JOIN m.checklists c WHERE c.id = :checklistId")
    List<Microservice> findByChecklistId(@Param("checklistId") UUID checklistId);
    
    @Query("SELECT m FROM Microservice m JOIN m.features f WHERE f.id = :featureId")
    List<Microservice> findByFeatureId(@Param("featureId") UUID featureId);
    
    long countByStatus(MicroserviceStatus status);
}
