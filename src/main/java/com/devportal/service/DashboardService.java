package com.devportal.service;

import com.devportal.domain.entity.Checklist;
import com.devportal.domain.entity.Microservice;
import com.devportal.domain.enums.ChecklistStatus;
import com.devportal.domain.enums.FeatureStatus;
import com.devportal.domain.enums.IssueCategory;
import com.devportal.domain.enums.IssueStatus;
import com.devportal.domain.enums.MicroserviceStatus;
import com.devportal.dto.response.ActivityLogResponse;
import com.devportal.dto.response.DashboardResponse;
import com.devportal.dto.response.DashboardResponse.HighImpactService;
import com.devportal.dto.response.DashboardResponse.TechnicalDebtService;
import com.devportal.repository.ChecklistRepository;
import com.devportal.repository.FeatureRepository;
import com.devportal.repository.IssueRepository;
import com.devportal.repository.MicroserviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FeatureRepository featureRepository;
    private final MicroserviceRepository microserviceRepository;
    private final ChecklistRepository checklistRepository;
    private final IssueRepository issueRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats() {
        long totalFeatures = featureRepository.count();
        long totalMicroservices = microserviceRepository.count();
        long totalChecklists = checklistRepository.countByIsActiveTrue();

        Map<String, Long> featuresByStatus = new HashMap<>();
        for (FeatureStatus status : FeatureStatus.values()) {
            featuresByStatus.put(status.name(), featureRepository.countByStatus(status));
        }

        Map<String, Long> microservicesByStatus = new HashMap<>();
        for (MicroserviceStatus status : MicroserviceStatus.values()) {
            microservicesByStatus.put(status.name(), microserviceRepository.countByStatus(status));
        }

        Map<String, Long> checklistsByStatus = new HashMap<>();
        for (ChecklistStatus status : ChecklistStatus.values()) {
            checklistsByStatus.put(status.name(), checklistRepository.countByIsActiveTrueAndStatus(status));
        }

        // Calculate overall progress
        long completedChecklists = checklistsByStatus.getOrDefault(ChecklistStatus.COMPLETED.name(), 0L);
        double overallProgress = totalChecklists > 0 ? 
                Math.round((completedChecklists * 100.0 / totalChecklists) * 100.0) / 100.0 : 0.0;

        List<ActivityLogResponse> recentActivities = activityLogService.getRecentActivities(10);

        List<HighImpactService> highImpactServices = getHighImpactServices();
        List<TechnicalDebtService> technicalDebtServices = getTechnicalDebtServices();

        // Issue category statistics
        Map<String, Long> issuesByCategory = new HashMap<>();
        try {
            List<Object[]> categoryResults = issueRepository.countGroupedByCategory();
            for (Object[] row : categoryResults) {
                if (row[0] != null) {
                    IssueCategory category = (IssueCategory) row[0];
                    Long count = (Long) row[1];
                    issuesByCategory.put(category.name(), count);
                }
            }
        } catch (Exception e) {
            // Log but don't fail dashboard
        }

        long totalTechDebtIssues = 0;
        long openTechDebtIssues = 0;
        try {
            totalTechDebtIssues = issueRepository.countByCategory(IssueCategory.TECH_DEBT);
            openTechDebtIssues = issueRepository.countByCategoryAndStatusNot(IssueCategory.TECH_DEBT, IssueStatus.COMPLETED);
        } catch (Exception e) {
            // Log but don't fail dashboard
        }

        return DashboardResponse.builder()
                .totalFeatures(totalFeatures)
                .totalMicroservices(totalMicroservices)
                .totalChecklists(totalChecklists)
                .featuresByStatus(featuresByStatus)
                .microservicesByStatus(microservicesByStatus)
                .checklistsByStatus(checklistsByStatus)
                .overallProgress(overallProgress)
                .recentActivities(recentActivities)
                .highImpactServices(highImpactServices)
                .technicalDebtServices(technicalDebtServices)
                .issuesByCategory(issuesByCategory)
                .totalTechDebtIssues(totalTechDebtIssues)
                .openTechDebtIssues(openTechDebtIssues)
                .build();
    }

    private List<HighImpactService> getHighImpactServices() {
        List<Microservice> allMicroservices = microserviceRepository.findAll();
        
        return allMicroservices.stream()
                .filter(ms -> ms.getHighRisk() != null && ms.getHighRisk())
                .sorted((a, b) -> {
                    int featureCountA = a.getFeatures() != null ? a.getFeatures().size() : 0;
                    int featureCountB = b.getFeatures() != null ? b.getFeatures().size() : 0;
                    return Integer.compare(featureCountB, featureCountA);
                })
                .limit(5)
                .map(ms -> {
                    int featureCount = ms.getFeatures() != null ? ms.getFeatures().size() : 0;
                    double progress = calculateMicroserviceProgress(ms);
                    return HighImpactService.builder()
                            .id(ms.getId().toString())
                            .name(ms.getName())
                            .featureCount(featureCount)
                            .progressPercentage(progress)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<TechnicalDebtService> getTechnicalDebtServices() {
        List<Microservice> allMicroservices = microserviceRepository.findAll();
        LocalDateTime fourteenDaysAgo = LocalDateTime.now().minusDays(14);
        
        return allMicroservices.stream()
                .filter(ms -> ms.getTechnicalDebtScore() != null && ms.getTechnicalDebtScore() > 0)
                .sorted((a, b) -> Integer.compare(
                        b.getTechnicalDebtScore() != null ? b.getTechnicalDebtScore() : 0,
                        a.getTechnicalDebtScore() != null ? a.getTechnicalDebtScore() : 0))
                .limit(5)
                .map(ms -> {
                    Set<Checklist> checklists = ms.getChecklists();
                    int blockedCount = 0;
                    int stalePendingCount = 0;
                    
                    if (checklists != null) {
                        for (Checklist c : checklists) {
                            if (c.getStatus() == ChecklistStatus.BLOCKED) {
                                blockedCount++;
                            }
                            if (c.getStatus() == ChecklistStatus.PLANNED && 
                                c.getCreatedAt() != null && 
                                c.getCreatedAt().isBefore(fourteenDaysAgo)) {
                                stalePendingCount++;
                            }
                        }
                    }
                    
                    return TechnicalDebtService.builder()
                            .id(ms.getId().toString())
                            .name(ms.getName())
                            .debtScore(ms.getTechnicalDebtScore() != null ? ms.getTechnicalDebtScore() : 0)
                            .blockedCount(blockedCount)
                            .stalePendingCount(stalePendingCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private double calculateMicroserviceProgress(Microservice ms) {
        if (ms.getChecklists() == null || ms.getChecklists().isEmpty()) return 0.0;
        long completed = ms.getChecklists().stream()
                .filter(c -> c.getStatus() == ChecklistStatus.COMPLETED)
                .count();
        return Math.round((completed * 100.0 / ms.getChecklists().size()) * 100.0) / 100.0;
    }
}
