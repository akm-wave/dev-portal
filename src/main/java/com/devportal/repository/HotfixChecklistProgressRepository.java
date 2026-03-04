package com.devportal.repository;

import com.devportal.domain.entity.HotfixChecklistProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HotfixChecklistProgressRepository extends JpaRepository<HotfixChecklistProgress, UUID> {
    
    List<HotfixChecklistProgress> findByHotfixIdOrderByCreatedAtAsc(UUID hotfixId);
    
    Optional<HotfixChecklistProgress> findByHotfixIdAndChecklistId(UUID hotfixId, UUID checklistId);
    
    void deleteByHotfixId(UUID hotfixId);
    
    long countByHotfixId(UUID hotfixId);
}
