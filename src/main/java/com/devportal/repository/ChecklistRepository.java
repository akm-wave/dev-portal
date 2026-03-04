package com.devportal.repository;

import com.devportal.domain.entity.Checklist;
import com.devportal.domain.enums.ChecklistPriority;
import com.devportal.domain.enums.ChecklistStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, UUID> {
    
    Page<Checklist> findByIsActiveTrue(Pageable pageable);
    
    Page<Checklist> findByIsActiveTrueAndStatus(ChecklistStatus status, Pageable pageable);
    
    Page<Checklist> findByIsActiveTrueAndPriority(ChecklistPriority priority, Pageable pageable);
    
    Page<Checklist> findByIsActiveTrueAndStatusAndPriority(ChecklistStatus status, ChecklistPriority priority, Pageable pageable);
    
    @Query("SELECT c FROM Checklist c WHERE c.isActive = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Checklist> searchByNameOrDescription(@Param("search") String search, Pageable pageable);
    
    List<Checklist> findByIdInAndIsActiveTrue(List<UUID> ids);
    
    long countByIsActiveTrue();
    
    long countByIsActiveTrueAndStatus(ChecklistStatus status);
    
    @Query("SELECT c FROM Checklist c JOIN c.microservices m WHERE m.id = :microserviceId AND c.isActive = true")
    List<Checklist> findByMicroserviceId(@Param("microserviceId") UUID microserviceId);

    List<Checklist> findByAssignedToIdAndIsActiveTrue(UUID userId);

    @Query("SELECT c FROM Checklist c WHERE c.assignedTo.id = :userId AND c.isActive = true AND c.status = :status")
    List<Checklist> findByAssignedToIdAndStatus(@Param("userId") UUID userId, @Param("status") ChecklistStatus status);

    @Query("SELECT COALESCE(SUM(c.weight), 0) FROM Checklist c WHERE c.assignedTo.id = :userId AND c.isActive = true")
    Integer sumWeightByAssignedToId(@Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(c.weight), 0) FROM Checklist c WHERE c.assignedTo.id = :userId AND c.isActive = true AND c.status = 'DONE'")
    Integer sumCompletedWeightByAssignedToId(@Param("userId") UUID userId);
}
