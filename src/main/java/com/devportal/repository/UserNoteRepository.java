package com.devportal.repository;

import com.devportal.domain.entity.UserNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserNoteRepository extends JpaRepository<UserNote, UUID> {

    Page<UserNote> findByUserIdAndIsArchivedFalseOrderByIsPinnedDescCreatedAtDesc(UUID userId, Pageable pageable);

    Page<UserNote> findByUserIdAndIsArchivedTrueOrderByUpdatedAtDesc(UUID userId, Pageable pageable);

    List<UserNote> findByUserIdAndIsPinnedTrueAndIsArchivedFalseOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT n FROM UserNote n WHERE n.user.id = :userId AND n.isArchived = false " +
           "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(n.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<UserNote> searchByUserIdAndKeyword(@Param("userId") UUID userId, @Param("search") String search, Pageable pageable);

    List<UserNote> findByUserIdAndModuleTypeAndModuleId(UUID userId, String moduleType, UUID moduleId);

    long countByUserIdAndIsArchivedFalse(UUID userId);

    long countByUserIdAndIsPinnedTrueAndIsArchivedFalse(UUID userId);
}
