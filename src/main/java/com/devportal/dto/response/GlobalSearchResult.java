package com.devportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSearchResult {
    private List<SearchItem> domains;
    private List<SearchItem> features;
    private List<SearchItem> microservices;
    private List<SearchItem> checklists;
    private List<SearchItem> incidents;
    private List<SearchItem> hotfixes;
    private List<SearchItem> issues;
    private List<SearchItem> utilities;
    private List<SearchItem> releases;
    private int totalCount;

    private List<SearchItem> attachments;
    private List<SearchItem> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchItem {
        private String id;
        private String name;
        private String description;
        private String type;
        private String status;
        private String url;
        private String contentSnippet;
        private String fileName;
        private String moduleType;
        private String moduleId;
    }
}
