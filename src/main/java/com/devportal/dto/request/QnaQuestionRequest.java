package com.devportal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaQuestionRequest {
    private String title;
    private String content;
    private List<String> tags;
    private List<HyperlinkRequest> hyperlinks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HyperlinkRequest {
        private String url;
        private String title;
    }
}
