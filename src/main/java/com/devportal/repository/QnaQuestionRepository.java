package com.devportal.repository;

import com.devportal.domain.entity.QnaQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QnaQuestionRepository extends JpaRepository<QnaQuestion, UUID> {

    Page<QnaQuestion> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT q FROM QnaQuestion q WHERE SIZE(q.answers) = 0 ORDER BY q.createdAt DESC")
    Page<QnaQuestion> findUnanswered(Pageable pageable);

    @Query("SELECT q FROM QnaQuestion q ORDER BY SIZE(q.answers) DESC, q.viewCount DESC")
    Page<QnaQuestion> findMostActive(Pageable pageable);

    @Query("SELECT q FROM QnaQuestion q WHERE q.tags LIKE %:tag% ORDER BY q.createdAt DESC")
    Page<QnaQuestion> findByTag(@Param("tag") String tag, Pageable pageable);

    @Query("SELECT q FROM QnaQuestion q WHERE q.createdBy.id = :userId ORDER BY q.createdAt DESC")
    Page<QnaQuestion> findByCreatedBy(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT q FROM QnaQuestion q WHERE " +
           "LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.tags) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY q.createdAt DESC")
    Page<QnaQuestion> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT q FROM QnaQuestion q WHERE " +
           "LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY q.createdAt DESC")
    List<QnaQuestion> searchForGlobal(@Param("keyword") String keyword);

    @Query("SELECT COUNT(q) FROM QnaQuestion q WHERE q.createdBy.id = :userId")
    long countByCreatedBy(@Param("userId") UUID userId);
}
