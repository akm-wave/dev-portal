package com.devportal.service;

import com.devportal.dto.response.WorkspaceProductivityDTO;
import com.devportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final EntityManager entityManager;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public WorkspaceProductivityDTO getMyProductivityDashboard(String dateRange) {
        UUID userId = getCurrentUserId();
        
        LocalDateTime[] dateRangeArray = parseDateRange(dateRange);
        LocalDateTime startDate = dateRangeArray[0];
        LocalDateTime endDate = dateRangeArray[1];
        LocalDateTime previousStartDate = dateRangeArray[2];
        LocalDateTime previousEndDate = dateRangeArray[3];

        log.info("Generating productivity dashboard for user {} from {} to {}", userId, startDate, endDate);

        // SECTION A - Active Work Counts
        Long activeFeatures = countActiveFeatures(userId, startDate, endDate);
        log.debug("Active Features: {}", activeFeatures);
        Long activeIncidents = countActiveIncidents(userId, startDate, endDate);
        log.debug("Active Incidents: {}", activeIncidents);
        Long activeHotfixes = countActiveHotfixes(userId, startDate, endDate);
        log.debug("Active Hotfixes: {}", activeHotfixes);
        Long activeIssues = countActiveIssues(userId, startDate, endDate);
        log.debug("Active Issues: {}", activeIssues);
        Long activeMicroservices = countActiveMicroservices(userId, startDate, endDate);
        log.debug("Active Microservices: {}", activeMicroservices);

        // SECTION A - Overdue Tasks
        List<Object[]> overdueTasksRaw = findOverdueTasks(userId);
        List<WorkspaceProductivityDTO.OverdueTaskDTO> overdueTasks = mapToOverdueTasks(overdueTasksRaw);
        Long overdueTasksCount = (long) overdueTasks.size();

        // SECTION B - Productivity Metrics
        Double completionRate = calculateCompletionRate(userId, startDate, endDate);
        log.debug("Completion Rate: {}%", completionRate);
        Double onTimeRate = calculateOnTimeRate(userId, startDate, endDate);
        log.debug("On-Time Rate: {}%", onTimeRate);
        Double avgResolutionTime = calculateAvgResolutionTime(userId, startDate, endDate);
        log.debug("Avg Resolution Time: {} hours", avgResolutionTime);
        
        Integer currentScore = calculateProductivityScore(userId, startDate, endDate);
        log.debug("Current Productivity Score: {}", currentScore);
        Integer previousScore = calculateProductivityScore(userId, previousStartDate, previousEndDate);
        log.debug("Previous Productivity Score: {}", previousScore);
        Double trendPercentage = calculateTrendPercentage(currentScore, previousScore);
        log.debug("Trend Percentage: {}%", trendPercentage);

        // Weekly Trend
        List<Object[]> weeklyTrendRaw = getWeeklyTrend(userId, previousStartDate, endDate);
        List<WorkspaceProductivityDTO.WeekScoreDTO> weeklyTrend = mapToWeeklyTrend(weeklyTrendRaw);

        // SECTION C - Recent Activities
        List<Object[]> recentActivitiesRaw = getRecentActivities(userId, startDate, endDate);
        List<WorkspaceProductivityDTO.ActivityDTO> recentActivities = mapToActivities(recentActivitiesRaw);

        return WorkspaceProductivityDTO.builder()
                // SECTION A
                .activeFeatures(activeFeatures)
                .activeIncidents(activeIncidents)
                .activeHotfixes(activeHotfixes)
                .activeIssues(activeIssues)
                .activeMicroservices(activeMicroservices)
                .overdueTasks(overdueTasksCount)
                .overdueItems(overdueTasks)
                
                // SECTION B
                .completionRate(completionRate)
                .onTimeRate(onTimeRate)
                .avgResolutionTime(avgResolutionTime)
                .productivityScore(currentScore)
                .previousPeriodScore(previousScore)
                .trendPercentage(trendPercentage)
                .weeklyTrend(weeklyTrend)
                
                // SECTION C
                .recentActivities(recentActivities)
                
                // Metadata
                .generatedAt(LocalDateTime.now())
                .dateRange(dateRange)
                .build();
    }

    private LocalDateTime[] parseDateRange(String dateRange) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate, endDate, previousStartDate, previousEndDate;
        
        switch (dateRange.toLowerCase()) {
            case "today":
                startDate = now.toLocalDate().atStartOfDay();
                endDate = now.toLocalDate().atTime(23, 59, 59);
                previousStartDate = startDate.minusDays(1);
                previousEndDate = endDate.minusDays(1);
                break;
                
            case "this_week":
                startDate = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
                endDate = startDate.plusDays(6).toLocalDate().atTime(23, 59, 59);
                previousStartDate = startDate.minusWeeks(1);
                previousEndDate = endDate.minusWeeks(1);
                break;
                
            case "this_month":
                startDate = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
                endDate = startDate.plusMonths(1).minusDays(1).toLocalDate().atTime(23, 59, 59);
                previousStartDate = startDate.minusMonths(1);
                previousEndDate = endDate.minusMonths(1);
                break;
                
            default:
                // Default to last 30 days
                endDate = now;
                startDate = now.minusDays(30);
                previousStartDate = startDate.minusDays(30);
                previousEndDate = startDate;
                break;
        }
        
        return new LocalDateTime[]{startDate, endDate, previousStartDate, previousEndDate};
    }

    private Double calculateCompletionRate(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        Long totalAssigned = countTotalAssignedItems(userId, startDate, endDate);
        Long completed = countCompletedItems(userId, startDate, endDate);
        
        if (totalAssigned == 0) return 0.0;
        return (double) completed / totalAssigned * 100;
    }

    private Double calculateOnTimeRate(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createNativeQuery(
            "SELECT " +
            "  CASE WHEN COUNT(*) = 0 THEN 0 " +
            "  ELSE (COUNT(CASE WHEN on_time THEN 1 END) * 100.0 / COUNT(*)) END " +
            "FROM ( " +
            "  SELECT CASE WHEN f.completed_at <= f.target_date THEN true ELSE false END as on_time " +
            "  FROM features f WHERE f.owner_id = :userId AND f.status IN ('COMPLETED', 'RELEASED') " +
            "  AND f.completed_at BETWEEN :startDate AND :endDate AND f.target_date IS NOT NULL " +
            "  UNION ALL " +
            "  SELECT CASE WHEN i.resolved_at <= i.due_date THEN true ELSE false END " +
            "  FROM issues i WHERE i.assigned_to = :userId AND i.status = 'COMPLETED' " +
            "  AND i.resolved_at BETWEEN :startDate AND :endDate AND i.due_date IS NOT NULL " +
            "  UNION ALL " +
            "  SELECT CASE WHEN inc.resolved_at <= inc.due_date THEN true ELSE false END " +
            "  FROM incidents inc WHERE inc.owner_id = :userId AND inc.status = 'COMPLETED' " +
            "  AND inc.resolved_at BETWEEN :startDate AND :endDate AND inc.due_date IS NOT NULL " +
            "  UNION ALL " +
            "  SELECT CASE WHEN h.deployed_at <= h.due_date THEN true ELSE false END " +
            "  FROM hotfixes h WHERE h.owner_id = :userId AND h.status = 'DEPLOYED' " +
            "  AND h.deployed_at BETWEEN :startDate AND :endDate AND h.due_date IS NOT NULL " +
            ") deliveries"
        );
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        Number result = (Number) query.getSingleResult();
        return result != null ? Math.round(result.doubleValue() * 10.0) / 10.0 : 0.0;
    }

    private Double calculateAvgResolutionTime(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createNativeQuery(
            "SELECT " +
            "  CASE WHEN COUNT(*) = 0 THEN 0 " +
            "  ELSE AVG(EXTRACT(EPOCH FROM (completed_at - created_at)) / 3600.0) END " +
            "FROM ( " +
            "  SELECT f.completed_at, f.created_at FROM features f " +
            "  WHERE f.owner_id = :userId AND f.status IN ('COMPLETED', 'RELEASED') " +
            "  AND f.completed_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT i.resolved_at, i.created_at FROM issues i " +
            "  WHERE i.assigned_to = :userId AND i.status = 'COMPLETED' " +
            "  AND i.resolved_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT inc.resolved_at, inc.created_at FROM incidents inc " +
            "  WHERE inc.owner_id = :userId AND inc.status = 'COMPLETED' " +
            "  AND inc.resolved_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT h.deployed_at, h.created_at FROM hotfixes h " +
            "  WHERE h.owner_id = :userId AND h.status = 'DEPLOYED' " +
            "  AND h.deployed_at BETWEEN :startDate AND :endDate " +
            ") resolutions"
        );
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        Number result = (Number) query.getSingleResult();
        return result != null ? Math.round(result.doubleValue() * 10.0) / 10.0 : 0.0;
    }

    
    private Double calculateTrendPercentage(Integer currentScore, Integer previousScore) {
        if (previousScore == 0) return currentScore > 0 ? 100.0 : 0.0;
        return ((double) (currentScore - previousScore) / previousScore) * 100;
    }

    private List<WorkspaceProductivityDTO.OverdueTaskDTO> mapToOverdueTasks(List<Object[]> rawData) {
        List<WorkspaceProductivityDTO.OverdueTaskDTO> result = new ArrayList<>();
        
        for (Object[] row : rawData) {
            result.add(WorkspaceProductivityDTO.OverdueTaskDTO.builder()
                    .id(row[0].toString())
                    .title((String) row[1])
                    .type((String) row[2])
                    .status((String) row[3])
                    .dueDate(row[4] != null ? ((java.sql.Timestamp) row[4]).toLocalDateTime() : null)
                    .createdAt(row[5] != null ? ((java.sql.Timestamp) row[5]).toLocalDateTime() : null)
                    .daysOverdue(row[6] != null ? ((Number) row[6]).intValue() : 0)
                    .priority((String) row[7])
                    .assignee((String) row[8])
                    .build());
        }
        
        return result;
    }

    private List<WorkspaceProductivityDTO.WeekScoreDTO> mapToWeeklyTrend(List<Object[]> rawData) {
        List<WorkspaceProductivityDTO.WeekScoreDTO> result = new ArrayList<>();
        
        for (Object[] row : rawData) {
            result.add(WorkspaceProductivityDTO.WeekScoreDTO.builder()
                    .weekStart(row[0].toString())
                    .weekEnd(row[1].toString())
                    .score(row[2] != null ? ((Number) row[2]).intValue() : 0)
                    .completedItems(row[3] != null ? ((Number) row[3]).intValue() : 0)
                    .build());
        }
        
        return result;
    }

    private List<WorkspaceProductivityDTO.ActivityDTO> mapToActivities(List<Object[]> rawData) {
        List<WorkspaceProductivityDTO.ActivityDTO> result = new ArrayList<>();
        
        for (Object[] row : rawData) {
            boolean onTime = false;
            if (row[5] != null) {
                onTime = (Boolean) row[5];
            }
            
            result.add(WorkspaceProductivityDTO.ActivityDTO.builder()
                    .id(row[0].toString())
                    .type((String) row[1])
                    .title((String) row[2])
                    .completedAt(row[3] != null ? ((java.sql.Timestamp) row[3]).toLocalDateTime() : null)
                    .dueDate(row[4] != null ? ((java.sql.Timestamp) row[4]).toLocalDateTime() : null)
                    .onTime(onTime)
                    .points(row[6] != null ? ((Number) row[6]).intValue() : 0)
                    .assignee((String) row[7])
                    .build());
        }
        
        return result;
    }

    private UUID getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(user -> user.getId())
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    // Helper methods using native queries
    private Long countActiveFeatures(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM features WHERE owner_id = :userId " +
            "AND status NOT IN ('COMPLETED', 'RELEASED') " +
            "AND created_at BETWEEN :startDate AND :endDate"
        );
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        log.debug("Counting active features for user {} between {} and {}", userId, startDate, endDate);
        return ((Number) query.getSingleResult()).longValue();
    }

    private Long countActiveIncidents(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM incidents WHERE owner_id = :userId " +
            "AND status NOT IN ('COMPLETED', 'CLOSED') " +
            "AND created_at BETWEEN :startDate AND :endDate"
        );
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return ((Number) query.getSingleResult()).longValue();
    }

    private Long countActiveHotfixes(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM hotfixes WHERE owner_id = :userId " +
            "AND status NOT IN ('DEPLOYED') " +
            "AND created_at BETWEEN :startDate AND :endDate"
        );
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        log.debug("Counting active hotfixes for user {} between {} and {}", userId, startDate, endDate);
        return ((Number) query.getSingleResult()).longValue();
    }

    private Long countActiveIssues(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM issues WHERE assigned_to = :userId " +
            "AND status NOT IN ('COMPLETED', 'CLOSED') " +
            "AND created_at BETWEEN :startDate AND :endDate"
        );
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return ((Number) query.getSingleResult()).longValue();
    }

    private Long countActiveMicroservices(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM microservices WHERE owner_id = :userId " +
            "AND created_at BETWEEN :startDate AND :endDate"
        );
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return ((Number) query.getSingleResult()).longValue();
    }

    private List<Object[]> findOverdueTasks(UUID userId) {
        Query query = entityManager.createNativeQuery(
            "SELECT f.id, f.name, 'FEATURE', f.status, f.target_date, f.created_at, " +
            "EXTRACT(DAY FROM (CURRENT_TIMESTAMP - f.target_date)), 'HIGH', u.username " +
            "FROM features f JOIN users u ON f.owner_id = u.id " +
            "WHERE f.owner_id = :userId AND f.target_date < CURRENT_TIMESTAMP " +
            "AND f.status NOT IN ('COMPLETED', 'RELEASED') " +
            "UNION ALL " +
            "SELECT i.id, i.title, 'INCIDENT', i.status, i.due_date, i.created_at, " +
            "EXTRACT(DAY FROM (CURRENT_TIMESTAMP - i.due_date)), " +
            "CASE WHEN i.severity = 'CRITICAL' THEN 'HIGH' WHEN i.severity = 'HIGH' THEN 'HIGH' ELSE 'MEDIUM' END, u.username " +
            "FROM incidents i JOIN users u ON i.owner_id = u.id " +
            "WHERE i.owner_id = :userId AND i.due_date < CURRENT_TIMESTAMP " +
            "AND i.status NOT IN ('COMPLETED', 'CLOSED') " +
            "UNION ALL " +
            "SELECT h.id, h.title, 'HOTFIX', h.status, h.due_date, h.created_at, " +
            "EXTRACT(DAY FROM (CURRENT_TIMESTAMP - h.due_date)), 'HIGH', u.username " +
            "FROM hotfixes h JOIN users u ON h.owner_id = u.id " +
            "WHERE h.owner_id = :userId AND h.due_date < CURRENT_TIMESTAMP " +
            "AND h.status NOT IN ('DEPLOYED', 'CANCELLED') " +
            "UNION ALL " +
            "SELECT i.id, i.title, 'ISSUE', i.status, i.due_date, i.created_at, " +
            "EXTRACT(DAY FROM (CURRENT_TIMESTAMP - i.due_date)), i.priority, u.username " +
            "FROM issues i JOIN users u ON i.assigned_to = u.id " +
            "WHERE i.assigned_to = :userId AND i.due_date < CURRENT_TIMESTAMP " +
            "AND i.status NOT IN ('COMPLETED', 'CLOSED')"
        );
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    private Integer calculateProductivityScore(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createNativeQuery(
            "SELECT COALESCE(SUM(score), 0) FROM ( " +
            "  SELECT COUNT(*) * 10 as score FROM features " +
            "  WHERE owner_id = :userId AND status IN ('COMPLETED', 'RELEASED') " +
            "  AND completed_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT COUNT(*) * 3 as score FROM issues " +
            "  WHERE assigned_to = :userId AND status = 'COMPLETED' " +
            "  AND resolved_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT COUNT(*) * 5 as score FROM incidents " +
            "  WHERE owner_id = :userId AND status = 'COMPLETED' " +
            "  AND resolved_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT COUNT(*) * 5 as score FROM hotfixes " +
            "  WHERE owner_id = :userId AND status = 'DEPLOYED' " +
            "  AND deployed_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT COUNT(*) * 1 as score FROM feature_checkpoints " +
            "  WHERE assigned_to_id = :userId AND status = 'COMPLETED' " +
            "  AND updated_at BETWEEN :startDate AND :endDate " +
            ") scores"
        );
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        Number result = (Number) query.getSingleResult();
        Integer score = result != null ? result.intValue() : 0;
        log.info("Calculated productivity score: {} for user {} from {} to {}", score, userId, startDate, endDate);
        return score;
    }

    private Long countTotalAssignedItems(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createNativeQuery(
            "SELECT COALESCE(SUM(cnt), 0) FROM ( " +
            "  SELECT COUNT(*) as cnt FROM features " +
            "  WHERE owner_id = :userId AND created_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT COUNT(*) as cnt FROM issues " +
            "  WHERE assigned_to = :userId AND created_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT COUNT(*) as cnt FROM incidents " +
            "  WHERE owner_id = :userId AND created_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT COUNT(*) as cnt FROM hotfixes " +
            "  WHERE owner_id = :userId AND created_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT COUNT(*) as cnt FROM feature_checkpoints " +
            "  WHERE assigned_to_id = :userId AND created_at BETWEEN :startDate AND :endDate " +
            ") counts"
        );
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        Long total = ((Number) query.getSingleResult()).longValue();
        log.debug("Total assigned items: {} for user {}", total, userId);
        return total;
    }

    private Long countCompletedItems(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createNativeQuery(
            "SELECT COALESCE(SUM(cnt), 0) FROM ( " +
            "  SELECT COUNT(*) as cnt FROM features " +
            "  WHERE owner_id = :userId AND status IN ('COMPLETED', 'RELEASED') " +
            "  AND completed_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT COUNT(*) as cnt FROM issues " +
            "  WHERE assigned_to = :userId AND status = 'COMPLETED' " +
            "  AND resolved_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT COUNT(*) as cnt FROM incidents " +
            "  WHERE owner_id = :userId AND status = 'COMPLETED' " +
            "  AND resolved_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT COUNT(*) as cnt FROM hotfixes " +
            "  WHERE owner_id = :userId AND status = 'DEPLOYED' " +
            "  AND deployed_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT COUNT(*) as cnt FROM feature_checkpoints " +
            "  WHERE assigned_to_id = :userId AND status = 'COMPLETED' " +
            "  AND updated_at BETWEEN :startDate AND :endDate " +
            ") counts"
        );
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        Long completed = ((Number) query.getSingleResult()).longValue();
        log.debug("Completed items: {} for user {}", completed, userId);
        return completed;
    }

    private List<Object[]> getWeeklyTrend(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createNativeQuery(
            "SELECT " +
            "  DATE_TRUNC('week', week_date) as week_start, " +
            "  DATE_TRUNC('week', week_date) + INTERVAL '6 days' as week_end, " +
            "  COALESCE(SUM(score), 0) as score, " +
            "  COALESCE(SUM(completed), 0) as completed_items " +
            "FROM ( " +
            "  SELECT completed_at as week_date, 10 as score, 1 as completed " +
            "  FROM features WHERE owner_id = :userId AND status IN ('COMPLETED', 'RELEASED') " +
            "  AND completed_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT resolved_at, 3, 1 FROM issues " +
            "  WHERE assigned_to = :userId AND status = 'COMPLETED' " +
            "  AND resolved_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT resolved_at, 5, 1 FROM incidents " +
            "  WHERE owner_id = :userId AND status = 'COMPLETED' " +
            "  AND resolved_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT deployed_at, 5, 1 FROM hotfixes " +
            "  WHERE owner_id = :userId AND status = 'DEPLOYED' " +
            "  AND deployed_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT updated_at, 1, 1 FROM feature_checkpoints " +
            "  WHERE assigned_to_id = :userId AND status = 'COMPLETED' " +
            "  AND updated_at BETWEEN :startDate AND :endDate " +
            ") activities " +
            "GROUP BY DATE_TRUNC('week', week_date) " +
            "ORDER BY week_start"
        );
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }

    private List<Object[]> getRecentActivities(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createNativeQuery(
            "SELECT id, title, type, completed_at, due_date, on_time, points, assignee FROM ( " +
            "  SELECT f.id, f.name as title, 'COMPLETED_FEATURE' as type, " +
            "    f.completed_at, f.target_date as due_date, " +
            "    CASE WHEN f.completed_at <= f.target_date THEN true ELSE false END as on_time, " +
            "    10 as points, u.username as assignee " +
            "  FROM features f JOIN users u ON f.owner_id = u.id " +
            "  WHERE f.owner_id = :userId AND f.status IN ('COMPLETED', 'RELEASED') " +
            "  AND f.completed_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT i.id, i.title, 'COMPLETED_ISSUE', i.resolved_at, i.due_date, " +
            "    CASE WHEN i.resolved_at <= i.due_date THEN true ELSE false END, " +
            "    3, u.username " +
            "  FROM issues i JOIN users u ON i.assigned_to = u.id " +
            "  WHERE i.assigned_to = :userId AND i.status = 'COMPLETED' " +
            "  AND i.resolved_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT inc.id, inc.title, 'COMPLETED_INCIDENT', inc.resolved_at, inc.due_date, " +
            "    CASE WHEN inc.resolved_at <= inc.due_date THEN true ELSE false END, " +
            "    5, u.username " +
            "  FROM incidents inc JOIN users u ON inc.owner_id = u.id " +
            "  WHERE inc.owner_id = :userId AND inc.status = 'COMPLETED' " +
            "  AND inc.resolved_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT h.id, h.title, 'DEPLOYED_HOTFIX', h.deployed_at, h.due_date, " +
            "    CASE WHEN h.deployed_at <= h.due_date THEN true ELSE false END, " +
            "    5, u.username " +
            "  FROM hotfixes h JOIN users u ON h.owner_id = u.id " +
            "  WHERE h.owner_id = :userId AND h.status = 'DEPLOYED' " +
            "  AND h.deployed_at BETWEEN :startDate AND :endDate " +
            "  UNION ALL " +
            "  SELECT fc.id, c.name, 'COMPLETED_CHECKPOINT', fc.updated_at, fc.due_date, " +
            "    CASE WHEN fc.updated_at <= fc.due_date THEN true ELSE false END, " +
            "    1, u.username " +
            "  FROM feature_checkpoints fc " +
            "  JOIN checklists c ON fc.checklist_id = c.id " +
            "  JOIN users u ON fc.assigned_to_id = u.id " +
            "  WHERE fc.assigned_to_id = :userId AND fc.status = 'COMPLETED' " +
            "  AND fc.updated_at BETWEEN :startDate AND :endDate " +
            ") activities " +
            "ORDER BY completed_at DESC " +
            "LIMIT 20"
        );
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
}
