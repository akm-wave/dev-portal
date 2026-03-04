package com.devportal.repository;

import com.devportal.domain.entity.QnaAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QnaAttachmentRepository extends JpaRepository<QnaAttachment, UUID> {

    List<QnaAttachment> findByQuestionId(UUID questionId);

    List<QnaAttachment> findByAnswerId(UUID answerId);

    List<QnaAttachment> findByCommentId(UUID commentId);

    void deleteByQuestionId(UUID questionId);

    void deleteByAnswerId(UUID answerId);
}
