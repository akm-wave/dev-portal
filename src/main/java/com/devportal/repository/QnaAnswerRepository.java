package com.devportal.repository;

import com.devportal.domain.entity.QnaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, UUID> {

    List<QnaAnswer> findByQuestionIdOrderByIsAcceptedDescCreatedAtAsc(UUID questionId);

    @Query("SELECT COUNT(a) FROM QnaAnswer a WHERE a.createdBy.id = :userId")
    long countByCreatedBy(@Param("userId") UUID userId);

    @Query("SELECT COUNT(a) FROM QnaAnswer a WHERE a.createdBy.id = :userId AND a.isAccepted = true")
    long countAcceptedByCreatedBy(@Param("userId") UUID userId);

    @Query("SELECT a FROM QnaAnswer a WHERE " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<QnaAnswer> searchForGlobal(@Param("keyword") String keyword);
}
