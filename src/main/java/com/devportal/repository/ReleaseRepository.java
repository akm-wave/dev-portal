package com.devportal.repository;

import com.devportal.domain.entity.Release;
import com.devportal.domain.enums.ReleaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReleaseRepository extends JpaRepository<Release, UUID> {

    Page<Release> findByStatus(ReleaseStatus status, Pageable pageable);

    @Query("SELECT r FROM Release r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.version) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Release> searchByNameOrVersion(@Param("search") String search, Pageable pageable);

    @Query("SELECT r FROM Release r WHERE r.status = :status AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.version) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Release> searchByNameOrVersionAndStatus(@Param("search") String search, 
                                                  @Param("status") ReleaseStatus status, 
                                                  Pageable pageable);

    Page<Release> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByNameAndVersion(String name, String version);
}
