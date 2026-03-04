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
public class QnaQuestionResponse {
    private String id;
    private String title;
    private String content;
    private List<String> tags;
    private int viewCount;
    private int upvotes;
    private boolean isResolved;
    private int answerCount;
    private UserSummary createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AttachmentResponse> attachments;
    private List<HyperlinkResponse> hyperlinks;
    private List<QnaAnswerResponse> answers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private String id;
        private String username;
        private String fullName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentResponse {
        private String id;
        private String fileName;
        private String fileType;
        private long fileSize;
        private String fileUrl;
        private LocalDateTime uploadedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HyperlinkResponse {
        private String id;
        private String url;
        private String title;
    }
}
