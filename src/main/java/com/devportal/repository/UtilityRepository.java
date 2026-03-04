package com.devportal.repository;

import com.devportal.domain.entity.Utility;
import com.devportal.domain.enums.UtilityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UtilityRepository extends JpaRepository<Utility, UUID> {
    
    Page<Utility> findByType(UtilityType type, Pageable pageable);
    
    @Query("SELECT u FROM Utility u WHERE " +
           "LOWER(u.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Utility> searchByTitleOrDescription(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT u FROM Utility u WHERE u.type = :type AND " +
           "(LOWER(u.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Utility> searchByTypeAndTitleOrDescription(@Param("type") UtilityType type, 
                                                     @Param("search") String search, 
                                                     Pageable pageable);
    
    List<Utility> findByCreatedById(UUID userId);
}
