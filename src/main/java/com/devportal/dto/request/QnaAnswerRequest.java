package com.devportal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaAnswerRequest {
    private UUID questionId;
    private String content;
    private List<QnaQuestionRequest.HyperlinkRequest> hyperlinks;
}
