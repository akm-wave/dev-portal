package com.devportal.repository;

import com.devportal.domain.entity.QnaComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QnaCommentRepository extends JpaRepository<QnaComment, UUID> {

    List<QnaComment> findByAnswerIdOrderByCreatedAtAsc(UUID answerId);

    @Query("SELECT c FROM QnaComment c WHERE " +
           "LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<QnaComment> searchForGlobal(@Param("keyword") String keyword);
}
