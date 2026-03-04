package com.devportal.service;

import com.devportal.domain.entity.IssueAttachment;
import com.devportal.domain.entity.IssueResolutionAttachment;
import com.devportal.domain.entity.UtilityAttachment;
import com.devportal.dto.response.GlobalSearchResult;
import com.devportal.dto.response.GlobalSearchResult.SearchItem;
import com.devportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    private final FeatureRepository featureRepository;
    private final MicroserviceRepository microserviceRepository;
    private final ChecklistRepository checklistRepository;
    private final IncidentRepository incidentRepository;
    private final HotfixRepository hotfixRepository;
    private final IssueRepository issueRepository;
    private final UtilityRepository utilityRepository;
    private final ReleaseRepository releaseRepository;
    private final UtilityAttachmentRepository utilityAttachmentRepository;
    private final IssueAttachmentRepository issueAttachmentRepository;
    private final IssueResolutionAttachmentRepository issueResolutionAttachmentRepository;
    private final QnaQuestionRepository qnaQuestionRepository;

    private static final int MAX_RESULTS_PER_CATEGORY = 10;

    @Transactional(readOnly = true)
    public GlobalSearchResult search(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return GlobalSearchResult.builder()
                    .domains(new ArrayList<>())
                    .features(new ArrayList<>())
                    .microservices(new ArrayList<>())
                    .checklists(new ArrayList<>())
                    .incidents(new ArrayList<>())
                    .hotfixes(new ArrayList<>())
                    .issues(new ArrayList<>())
                    .utilities(new ArrayList<>())
                    .releases(new ArrayList<>())
                    .attachments(new ArrayList<>())
                    .totalCount(0)
                    .build();
        }

        String searchTerm = query.toLowerCase().trim();
        int maxResults = Math.min(limit, MAX_RESULTS_PER_CATEGORY);

        List<SearchItem> domains = searchDomains(searchTerm, maxResults);
        List<SearchItem> features = searchFeatures(searchTerm, maxResults);
        List<SearchItem> microservices = searchMicroservices(searchTerm, maxResults);
        List<SearchItem> checklists = searchChecklists(searchTerm, maxResults);
        List<SearchItem> incidents = searchIncidents(searchTerm, maxResults);
        List<SearchItem> hotfixes = searchHotfixes(searchTerm, maxResults);
        List<SearchItem> issues = searchIssues(searchTerm, maxResults);
        List<SearchItem> utilities = searchUtilities(searchTerm, maxResults);
        List<SearchItem> releases = searchReleases(searchTerm, maxResults);
        List<SearchItem> attachments = searchAttachmentContent(searchTerm, maxResults);
        List<SearchItem> questions = searchQnaQuestions(searchTerm, maxResults);

        int totalCount = domains.size() + features.size() + microservices.size() + checklists.size() 
                + incidents.size() + hotfixes.size() + issues.size() + utilities.size() + releases.size()
                + attachments.size() + questions.size();

        return GlobalSearchResult.builder()
                .domains(domains)
                .features(features)
                .microservices(microservices)
                .checklists(checklists)
                .incidents(incidents)
                .hotfixes(hotfixes)
                .issues(issues)
                .utilities(utilities)
                .releases(releases)
                .attachments(attachments)
                .questions(questions)
                .totalCount(totalCount)
                .build();
    }

    private List<SearchItem> searchDomains(String searchTerm, int limit) {
        Set<String> domains = new HashSet<>();
        featureRepository.findAll().forEach(f -> {
            if (f.getDomain() != null) domains.add(f.getDomain());
        });
        
        return domains.stream()
                .filter(d -> fuzzyMatch(d, searchTerm))
                .limit(limit)
                .map(d -> SearchItem.builder()
                        .id(d)
                        .name(d)
                        .description("Domain grouping features")
                        .type("domain")
                        .status("ACTIVE")
                        .url("/relationships?domain=" + d)
                        .build())
                .toList();
    }

    private List<SearchItem> searchFeatures(String pattern, int limit) {
        return featureRepository.findAll().stream()
                .filter(f -> matchesPattern(f.getName(), pattern) || matchesPattern(f.getDescription(), pattern))
                .limit(limit)
                .map(f -> SearchItem.builder()
                        .id(f.getId().toString())
                        .name(f.getName())
                        .description(truncate(f.getDescription(), 100))
                        .type("feature")
                        .status(f.getStatus().name())
                        .url("/features/" + f.getId())
                        .build())
                .toList();
    }

    private List<SearchItem> searchMicroservices(String pattern, int limit) {
        return microserviceRepository.findAll().stream()
                .filter(m -> matchesPattern(m.getName(), pattern) || matchesPattern(m.getDescription(), pattern))
                .limit(limit)
                .map(m -> SearchItem.builder()
                        .id(m.getId().toString())
                        .name(m.getName())
                        .description(truncate(m.getDescription(), 100))
                        .type("microservice")
                        .status(m.getStatus().name())
                        .url("/microservices/" + m.getId())
                        .build())
                .toList();
    }

    private List<SearchItem> searchChecklists(String pattern, int limit) {
        return checklistRepository.findAll().stream()
                .filter(c -> matchesPattern(c.getName(), pattern) || matchesPattern(c.getDescription(), pattern))
                .limit(limit)
                .map(c -> SearchItem.builder()
                        .id(c.getId().toString())
                        .name(c.getName())
                        .description(truncate(c.getDescription(), 100))
                        .type("checklist")
                        .status(c.getStatus().name())
                        .url("/checklists/" + c.getId())
                        .build())
                .toList();
    }

    private List<SearchItem> searchIncidents(String pattern, int limit) {
        return incidentRepository.findAll().stream()
                .filter(i -> matchesPattern(i.getTitle(), pattern) || matchesPattern(i.getDescription(), pattern))
                .limit(limit)
                .map(i -> SearchItem.builder()
                        .id(i.getId().toString())
                        .name(i.getTitle())
                        .description(truncate(i.getDescription(), 100))
                        .type("incident")
                        .status(i.getStatus().name())
                        .url("/incidents/" + i.getId())
                        .build())
                .toList();
    }

    private List<SearchItem> searchHotfixes(String pattern, int limit) {
        return hotfixRepository.findAll().stream()
                .filter(h -> matchesPattern(h.getTitle(), pattern) || matchesPattern(h.getDescription(), pattern))
                .limit(limit)
                .map(h -> SearchItem.builder()
                        .id(h.getId().toString())
                        .name(h.getTitle())
                        .description(truncate(h.getDescription(), 100))
                        .type("hotfix")
                        .status(h.getStatus().name())
                        .url("/hotfixes/" + h.getId())
                        .build())
                .toList();
    }

    private List<SearchItem> searchIssues(String pattern, int limit) {
        return issueRepository.findAll().stream()
                .filter(i -> matchesPattern(i.getTitle(), pattern) || matchesPattern(i.getDescription(), pattern))
                .limit(limit)
                .map(i -> SearchItem.builder()
                        .id(i.getId().toString())
                        .name(i.getTitle())
                        .description(truncate(i.getDescription(), 100))
                        .type("issue")
                        .status(i.getStatus().name())
                        .url("/issues/" + i.getId())
                        .build())
                .toList();
    }

    private List<SearchItem> searchUtilities(String searchTerm, int limit) {
        return utilityRepository.findAll().stream()
                .filter(u -> fuzzyMatch(u.getTitle(), searchTerm) || fuzzyMatch(u.getDescription(), searchTerm))
                .limit(limit)
                .map(u -> SearchItem.builder()
                        .id(u.getId().toString())
                        .name(u.getTitle())
                        .description(truncate(u.getDescription(), 100))
                        .type("utility")
                        .status(u.getType().name())
                        .url("/utilities/" + u.getId())
                        .build())
                .toList();
    }

    private List<SearchItem> searchReleases(String searchTerm, int limit) {
        return releaseRepository.findAll().stream()
                .filter(r -> fuzzyMatch(r.getName(), searchTerm) || fuzzyMatch(r.getVersion(), searchTerm) || fuzzyMatch(r.getDescription(), searchTerm))
                .limit(limit)
                .map(r -> SearchItem.builder()
                        .id(r.getId().toString())
                        .name(r.getName() + " " + r.getVersion())
                        .description(truncate(r.getDescription(), 100))
                        .type("release")
                        .status(r.getStatus().name())
                        .url("/releases/" + r.getId())
                        .build())
                .toList();
    }

    private boolean matchesPattern(String text, String pattern) {
        if (text == null) return false;
        return fuzzyMatch(text, pattern);
    }

    private boolean fuzzyMatch(String text, String searchTerm) {
        if (text == null || searchTerm == null) return false;
        String textLower = text.toLowerCase();
        String termLower = searchTerm.toLowerCase();
        
        // Exact contains match
        if (textLower.contains(termLower)) return true;
        
        // Fuzzy match: check if all characters of search term appear in order
        int termIndex = 0;
        for (int i = 0; i < textLower.length() && termIndex < termLower.length(); i++) {
            if (textLower.charAt(i) == termLower.charAt(termIndex)) {
                termIndex++;
            }
        }
        if (termIndex == termLower.length()) return true;
        
        // Levenshtein-like tolerance: allow 1-2 character differences for short terms
        if (termLower.length() >= 3) {
            // Check if any word in text starts with search term
            String[] words = textLower.split("\\s+");
            for (String word : words) {
                if (word.startsWith(termLower) || termLower.startsWith(word)) return true;
                // Allow 1 character difference for words >= 4 chars
                if (word.length() >= 4 && termLower.length() >= 4) {
                    int diff = levenshteinDistance(word.substring(0, Math.min(word.length(), termLower.length())), 
                                                    termLower.substring(0, Math.min(word.length(), termLower.length())));
                    if (diff <= 1) return true;
                }
            }
        }
        
        return false;
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[s1.length()][s2.length()];
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    private List<SearchItem> searchAttachmentContent(String searchTerm, int limit) {
        List<SearchItem> results = new ArrayList<>();
        
        // Search utility attachments
        utilityAttachmentRepository.findAll().stream()
                .filter(a -> matchesAttachmentContent(a.getFileName(), a.getContentIndex(), searchTerm))
                .limit(limit)
                .forEach(a -> {
                    String snippet = extractSnippet(a.getContentIndex(), searchTerm);
                    results.add(SearchItem.builder()
                            .id(a.getId().toString())
                            .name(a.getFileName())
                            .description("Utility attachment")
                            .type("attachment")
                            .status("INDEXED")
                            .url("/utilities/" + a.getUtility().getId())
                            .contentSnippet(snippet)
                            .fileName(a.getFileName())
                            .moduleType("UTILITY")
                            .moduleId(a.getUtility().getId().toString())
                            .build());
                });
        
        // Search issue attachments
        issueAttachmentRepository.findAll().stream()
                .filter(a -> matchesAttachmentContent(a.getFileName(), a.getContentIndex(), searchTerm))
                .limit(limit)
                .forEach(a -> {
                    String snippet = extractSnippet(a.getContentIndex(), searchTerm);
                    results.add(SearchItem.builder()
                            .id(a.getId().toString())
                            .name(a.getFileName())
                            .description("Issue attachment")
                            .type("attachment")
                            .status("INDEXED")
                            .url("/issues/" + a.getIssue().getId())
                            .contentSnippet(snippet)
                            .fileName(a.getFileName())
                            .moduleType("ISSUE")
                            .moduleId(a.getIssue().getId().toString())
                            .build());
                });
        
        // Search issue resolution attachments
        issueResolutionAttachmentRepository.findAll().stream()
                .filter(a -> matchesAttachmentContent(a.getFileName(), a.getContentIndex(), searchTerm))
                .limit(limit)
                .forEach(a -> {
                    String snippet = extractSnippet(a.getContentIndex(), searchTerm);
                    results.add(SearchItem.builder()
                            .id(a.getId().toString())
                            .name(a.getFileName())
                            .description("Issue resolution attachment")
                            .type("attachment")
                            .status("INDEXED")
                            .url("/issues/" + a.getIssueResolution().getIssue().getId())
                            .contentSnippet(snippet)
                            .fileName(a.getFileName())
                            .moduleType("ISSUE_RESOLUTION")
                            .moduleId(a.getIssueResolution().getId().toString())
                            .build());
                });
        
        return results.stream().limit(limit).toList();
    }

    private boolean matchesAttachmentContent(String fileName, String contentIndex, String searchTerm) {
        if (matchesPattern(fileName, searchTerm)) return true;
        if (contentIndex != null && contentIndex.toLowerCase().contains(searchTerm.toLowerCase())) return true;
        return false;
    }

    private String extractSnippet(String content, String searchTerm) {
        if (content == null || searchTerm == null) return null;
        
        String lowerContent = content.toLowerCase();
        String lowerTerm = searchTerm.toLowerCase();
        int index = lowerContent.indexOf(lowerTerm);
        
        if (index == -1) return truncate(content, 150);
        
        int start = Math.max(0, index - 50);
        int end = Math.min(content.length(), index + searchTerm.length() + 100);
        
        String snippet = content.substring(start, end);
        if (start > 0) snippet = "..." + snippet;
        if (end < content.length()) snippet = snippet + "...";
        
        return snippet;
    }

    private List<SearchItem> searchQnaQuestions(String searchTerm, int limit) {
        return qnaQuestionRepository.searchForGlobal(searchTerm).stream()
                .limit(limit)
                .map(q -> SearchItem.builder()
                        .id(q.getId().toString())
                        .name(q.getTitle())
                        .description(truncate(q.getContent(), 100))
                        .type("question")
                        .status(q.getIsResolved() ? "RESOLVED" : "OPEN")
                        .url("/qna/" + q.getId())
                        .build())
                .toList();
    }
}
