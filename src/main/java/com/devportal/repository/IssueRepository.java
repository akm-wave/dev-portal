package com.devportal.repository;

import com.devportal.domain.entity.Issue;
import com.devportal.domain.enums.IssueCategory;
import com.devportal.domain.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {

    Page<Issue> findByStatus(IssueStatus status, Pageable pageable);

    List<Issue> findByMainFeatureId(UUID featureId);

    @Query("SELECT i FROM Issue i WHERE i.assignedTo.id = :userId")
    List<Issue> findByAssignedToId(@Param("userId") UUID userId);

    @Query("SELECT i FROM Issue i WHERE i.owner.id = :ownerId")
    List<Issue> findByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("SELECT COUNT(i) FROM Issue i WHERE i.mainFeature.id = :featureId")
    long countByMainFeatureId(@Param("featureId") UUID featureId);

    @Query("SELECT i.status, COUNT(i) FROM Issue i WHERE i.assignedTo.id = :userId GROUP BY i.status")
    List<Object[]> countByUserGroupedByStatus(@Param("userId") UUID userId);

    List<Issue> findByStatusIn(List<IssueStatus> statuses);

    @Query("SELECT i.category, COUNT(i) FROM Issue i GROUP BY i.category")
    List<Object[]> countGroupedByCategory();

    long countByCategory(IssueCategory category);

    long countByCategoryAndStatusNot(IssueCategory category, IssueStatus status);

    @Query("""
            SELECT i
            FROM Issue i
            WHERE (:status IS NULL OR i.status = :status)
              AND (
                :search IS NULL OR :search = ''
                OR LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<Issue> findAllWithFilters(
            @Param("status") IssueStatus status,
            @Param("search") String search,
            Pageable pageable);
}
