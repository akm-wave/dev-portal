package com.devportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaAnswerResponse {
    private String id;
    private String questionId;
    private String content;
    private int upvotes;
    private boolean isAccepted;
    private QnaQuestionResponse.UserSummary createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QnaQuestionResponse.AttachmentResponse> attachments;
    private List<QnaQuestionResponse.HyperlinkResponse> hyperlinks;
    private List<QnaCommentResponse> comments;
}
