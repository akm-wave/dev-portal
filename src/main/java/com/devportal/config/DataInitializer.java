package com.devportal.config;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.*;
import com.devportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ChecklistRepository checklistRepository;
    private final MicroserviceRepository microserviceRepository;
    private final FeatureRepository featureRepository;
    private final IncidentRepository incidentRepository;
    private final HotfixRepository hotfixRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        if (checklistRepository.count() > 0) {
            log.info("Database already contains data, skipping initialization");
            return;
        }

        log.info("Initializing production-grade data for Dev Tracking Portal...");

        List<Checklist> checklists = createChecklists();
        List<Microservice> microservices = createMicroservices(checklists);
        List<Feature> features = createFeatures(microservices);
        List<Incident> incidents = createIncidents(features, microservices);
        List<Hotfix> hotfixes = createHotfixes(features, microservices);
        List<Issue> issues = createIssues(features);

        updateHighRiskAndDebtScores(microservices);

        log.info("Production data initialization completed!");
        log.info("Created {} checklists, {} microservices, {} features, {} incidents, {} hotfixes, {} issues",
                checklists.size(), microservices.size(), features.size(), 
                incidents.size(), hotfixes.size(), issues.size());
    }

    private List<Checklist> createChecklists() {
        String[] checklistNames = {
                "API contract finalized",
                "Database migration completed",
                "Unit test coverage above 80%",
                "Integration test passed",
                "Security review approved",
                "Load testing completed",
                "Logging standardized",
                "Monitoring configured",
                "Swagger documentation updated",
                "Deployment script verified",
                "CI/CD pipeline configured",
                "Docker image built",
                "Code review completed",
                "Performance benchmark completed",
                "Rollback plan prepared",
                "Error handling implemented",
                "Rate limiting configured",
                "Cache strategy implemented",
                "Database indexes optimized",
                "API versioning implemented",
                "Health check endpoint added",
                "Secrets management configured",
                "Input validation completed",
                "CORS configuration verified",
                "Timeout settings configured",
                "Retry logic implemented",
                "Circuit breaker configured",
                "Metrics collection enabled",
                "Alert rules configured",
                "Disaster recovery tested"
        };

        String[] descriptions = {
                "Ensure all API contracts are documented and agreed upon by stakeholders",
                "Complete database schema changes and data migration scripts",
                "Achieve minimum 80% code coverage with meaningful unit tests",
                "All integration tests must pass in staging environment",
                "Security team must approve all changes before deployment",
                "System must handle expected load with acceptable response times",
                "Implement structured logging with correlation IDs",
                "Set up Prometheus/Grafana monitoring dashboards",
                "Update OpenAPI/Swagger documentation for all endpoints",
                "Verify deployment scripts work in all environments",
                "Configure automated build, test, and deployment pipeline",
                "Build and push Docker images to container registry",
                "All code changes reviewed and approved by senior developers",
                "Benchmark performance against baseline metrics",
                "Document and test rollback procedures",
                "Implement comprehensive error handling and user-friendly messages",
                "Configure API rate limiting to prevent abuse",
                "Implement caching for frequently accessed data",
                "Add appropriate database indexes for query optimization",
                "Implement API versioning strategy for backward compatibility",
                "Add health check endpoints for load balancer integration",
                "Configure secure secrets management (Vault/AWS Secrets)",
                "Validate all user inputs to prevent injection attacks",
                "Configure CORS for allowed origins",
                "Set appropriate timeout values for all external calls",
                "Implement retry logic with exponential backoff",
                "Configure circuit breaker for fault tolerance",
                "Enable metrics collection for observability",
                "Configure alerting rules for critical metrics",
                "Test disaster recovery procedures"
        };

        ChecklistStatus[] statuses = ChecklistStatus.values();
        ChecklistPriority[] priorities = ChecklistPriority.values();

        List<Checklist> checklists = new ArrayList<>();

        for (int i = 0; i < checklistNames.length; i++) {
            ChecklistStatus status = getWeightedStatus();
            ChecklistPriority priority = priorities[random.nextInt(priorities.length)];

            Checklist checklist = Checklist.builder()
                    .name(checklistNames[i])
                    .description(descriptions[i])
                    .status(status)
                    .priority(priority)
                    .createdBy("admin")
                    .isActive(true)
                    .build();

            setRandomCreatedAt(checklist, 30);
            checklists.add(checklistRepository.save(checklist));
        }

        log.info("Created {} checklists", checklists.size());
        return checklists;
    }

    private ChecklistStatus getWeightedStatus() {
        int rand = random.nextInt(100);
        if (rand < 35) return ChecklistStatus.DONE;
        if (rand < 60) return ChecklistStatus.IN_PROGRESS;
        if (rand < 85) return ChecklistStatus.PENDING;
        return ChecklistStatus.BLOCKED;
    }

    private List<Microservice> createMicroservices(List<Checklist> allChecklists) {
        String[][] microserviceData = {
                {"auth-service", "Authentication and authorization service handling JWT tokens, OAuth2, and session management", "1.5.2", "Security Team"},
                {"kyc-service", "Know Your Customer service for identity verification and compliance checks", "2.1.0", "Compliance Team"},
                {"wallet-service", "Digital wallet management service for balance, transactions, and transfers", "3.0.1", "Payments Team"},
                {"payment-service", "Payment processing service integrating with multiple payment gateways", "2.8.0", "Payments Team"},
                {"notification-service", "Multi-channel notification service (Email, SMS, Push, In-App)", "1.9.3", "Platform Team"},
                {"fraud-engine", "Real-time fraud detection and prevention engine using ML models", "1.2.0", "Risk Team"},
                {"reporting-service", "Business intelligence and reporting service with scheduled reports", "2.0.5", "Analytics Team"},
                {"user-profile-service", "User profile management including preferences and settings", "1.7.1", "User Experience Team"},
                {"admin-service", "Administrative dashboard backend for system management", "1.4.0", "Platform Team"},
                {"audit-service", "Audit logging and compliance tracking service", "1.1.2", "Compliance Team"}
        };

        MicroserviceStatus[] statuses = {
                MicroserviceStatus.COMPLETED,
                MicroserviceStatus.IN_PROGRESS,
                MicroserviceStatus.IN_PROGRESS,
                MicroserviceStatus.IN_PROGRESS,
                MicroserviceStatus.COMPLETED,
                MicroserviceStatus.IN_PROGRESS,
                MicroserviceStatus.NOT_STARTED,
                MicroserviceStatus.IN_PROGRESS,
                MicroserviceStatus.NOT_STARTED,
                MicroserviceStatus.COMPLETED
        };

        List<Microservice> microservices = new ArrayList<>();
        List<Checklist> shuffledChecklists = new ArrayList<>(allChecklists);

        for (int i = 0; i < microserviceData.length; i++) {
            String[] data = microserviceData[i];

            Collections.shuffle(shuffledChecklists);
            int checklistCount = 2 + random.nextInt(4);
            Set<Checklist> msChecklists = new HashSet<>(shuffledChecklists.subList(0, Math.min(checklistCount, shuffledChecklists.size())));

            Microservice microservice = Microservice.builder()
                    .name(data[0])
                    .description(data[1])
                    .version(data[2])
                    .status(statuses[i])
                    .checklists(msChecklists)
                    .highRisk(false)
                    .technicalDebtScore(0)
                    .build();

            setRandomCreatedAt(microservice, 25);
            microservices.add(microserviceRepository.save(microservice));
        }

        log.info("Created {} microservices", microservices.size());
        return microservices;
    }

    private List<Feature> createFeatures(List<Microservice> allMicroservices) {
        Map<String, Microservice> msMap = new HashMap<>();
        for (Microservice ms : allMicroservices) {
            msMap.put(ms.getName(), ms);
        }

        Object[][] featureData = {
                {
                        "User Onboarding Enhancement",
                        "Streamline the user registration and onboarding process with improved UX and faster KYC verification",
                        "v2.5.0",
                        LocalDate.now().plusDays(30),
                        FeatureStatus.IN_PROGRESS,
                        "User Experience",
                        new String[]{"auth-service", "kyc-service", "user-profile-service", "notification-service"}
                },
                {
                        "Payment Gateway Upgrade",
                        "Upgrade payment infrastructure to support new payment methods and improve transaction success rates",
                        "v3.0.0",
                        LocalDate.now().plusDays(45),
                        FeatureStatus.IN_PROGRESS,
                        "Payments",
                        new String[]{"payment-service", "wallet-service", "notification-service", "fraud-engine"}
                },
                {
                        "Wallet Performance Optimization",
                        "Optimize wallet service for higher throughput and lower latency during peak hours",
                        "v3.1.0",
                        LocalDate.now().plusDays(20),
                        FeatureStatus.PLANNED,
                        "Wallet",
                        new String[]{"wallet-service", "audit-service"}
                },
                {
                        "Fraud Detection Improvement",
                        "Enhance fraud detection algorithms with new ML models and real-time scoring",
                        "v1.5.0",
                        LocalDate.now().plusDays(60),
                        FeatureStatus.IN_PROGRESS,
                        "Fraud",
                        new String[]{"fraud-engine", "payment-service", "notification-service", "audit-service"}
                },
                {
                        "KYC Automation Release",
                        "Automate KYC verification process with AI-powered document verification",
                        "v2.2.0",
                        LocalDate.now().minusDays(5),
                        FeatureStatus.RELEASED,
                        "KYC",
                        new String[]{"kyc-service", "user-profile-service", "notification-service"}
                },
                {
                        "Admin Portal Revamp",
                        "Complete redesign of admin portal with new dashboard, reporting, and user management features",
                        "v2.0.0",
                        LocalDate.now().plusDays(90),
                        FeatureStatus.PLANNED,
                        "Admin",
                        new String[]{"admin-service", "reporting-service", "audit-service", "auth-service"}
                }
        };

        List<Feature> features = new ArrayList<>();

        for (Object[] data : featureData) {
            String name = (String) data[0];
            String description = (String) data[1];
            String releaseVersion = (String) data[2];
            LocalDate targetDate = (LocalDate) data[3];
            FeatureStatus status = (FeatureStatus) data[4];
            String domain = (String) data[5];
            String[] microserviceNames = (String[]) data[6];

            Set<Microservice> featureMicroservices = new HashSet<>();
            for (String msName : microserviceNames) {
                Microservice ms = msMap.get(msName);
                if (ms != null) {
                    featureMicroservices.add(ms);
                }
            }

            Feature feature = Feature.builder()
                    .name(name)
                    .description(description)
                    .releaseVersion(releaseVersion)
                    .targetDate(targetDate)
                    .status(status)
                    .domain(domain)
                    .microservices(featureMicroservices)
                    .build();

            setRandomCreatedAt(feature, 20);
            features.add(featureRepository.save(feature));
        }

        log.info("Created {} features", features.size());
        return features;
    }

    private void updateHighRiskAndDebtScores(List<Microservice> microservices) {
        for (Microservice ms : microservices) {
            Microservice freshMs = microserviceRepository.findById(ms.getId()).orElse(ms);

            long featureCount = featureRepository.countByMicroserviceId(freshMs.getId());
            freshMs.setHighRisk(featureCount >= 3);

            int debtScore = calculateTechnicalDebtScore(freshMs);
            freshMs.setTechnicalDebtScore(debtScore);

            microserviceRepository.save(freshMs);

            if (freshMs.getHighRisk()) {
                log.info("Marked {} as HIGH RISK (used in {} features)", freshMs.getName(), featureCount);
            }
            if (debtScore > 0) {
                log.info("Technical debt score for {}: {}", freshMs.getName(), debtScore);
            }
        }
    }

    private int calculateTechnicalDebtScore(Microservice microservice) {
        int score = 0;
        LocalDateTime fourteenDaysAgo = LocalDateTime.now().minusDays(14);

        for (Checklist checklist : microservice.getChecklists()) {
            if (checklist.getStatus() == ChecklistStatus.BLOCKED) {
                score += 10;
            }

            if (checklist.getStatus() == ChecklistStatus.PENDING &&
                    checklist.getCreatedAt() != null &&
                    checklist.getCreatedAt().isBefore(fourteenDaysAgo)) {
                score += 5;
            }
        }

        return score;
    }

    private void setRandomCreatedAt(Object entity, int maxDaysAgo) {
        int daysAgo = random.nextInt(maxDaysAgo);
        int hoursAgo = random.nextInt(24);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(daysAgo).minusHours(hoursAgo);

        if (entity instanceof Checklist) {
            ((Checklist) entity).setCreatedAt(createdAt);
            ((Checklist) entity).setUpdatedAt(createdAt);
        } else if (entity instanceof Microservice) {
            ((Microservice) entity).setCreatedAt(createdAt);
            ((Microservice) entity).setUpdatedAt(createdAt);
        } else if (entity instanceof Feature) {
            ((Feature) entity).setCreatedAt(createdAt);
            ((Feature) entity).setUpdatedAt(createdAt);
        } else if (entity instanceof Incident) {
            ((Incident) entity).setCreatedAt(createdAt);
            ((Incident) entity).setUpdatedAt(createdAt);
        } else if (entity instanceof Hotfix) {
            ((Hotfix) entity).setCreatedAt(createdAt);
            ((Hotfix) entity).setUpdatedAt(createdAt);
        } else if (entity instanceof Issue) {
            ((Issue) entity).setCreatedAt(createdAt);
            ((Issue) entity).setUpdatedAt(createdAt);
        }
    }

    private List<Incident> createIncidents(List<Feature> features, List<Microservice> microservices) {
        Object[][] incidentData = {
                {
                        "Payment Gateway Timeout",
                        "Multiple users reporting payment failures due to gateway timeout errors. Transaction success rate dropped to 85% from normal 99.5%.",
                        Severity.HIGH,
                        IncidentStatus.RESOLVED,
                        new String[]{"payment-service"},
                        "Payment Gateway Upgrade"
                },
                {
                        "KYC Verification Delays",
                        "Document verification taking longer than expected (>30 mins vs normal 5 mins). ML model inference latency increased significantly.",
                        Severity.MEDIUM,
                        IncidentStatus.IN_PROGRESS,
                        new String[]{"kyc-service"},
                        "KYC Automation Release"
                },
                {
                        "Wallet Balance Sync Issue",
                        "Some users seeing incorrect wallet balance after transactions. Database replication lag detected between primary and read replicas.",
                        Severity.HIGH,
                        IncidentStatus.RESOLVED,
                        new String[]{"wallet-service"},
                        "Wallet Performance Optimization"
                },
                {
                        "Notification Service Outage",
                        "Push notifications not being delivered to iOS devices. APNs certificate expired causing delivery failures.",
                        Severity.HIGH,
                        IncidentStatus.RESOLVED,
                        new String[]{"notification-service"},
                        "User Onboarding Enhancement"
                },
                {
                        "Fraud Detection False Positives",
                        "Increased false positive rate (15% vs normal 2%) blocking legitimate transactions. Recent ML model update causing issues.",
                        Severity.MEDIUM,
                        IncidentStatus.IN_PROGRESS,
                        new String[]{"fraud-engine"},
                        "Fraud Detection Improvement"
                },
                {
                        "Auth Service Memory Leak",
                        "Authentication service experiencing memory leak causing periodic restarts. JWT token cache not being cleared properly.",
                        Severity.HIGH,
                        IncidentStatus.IN_PROGRESS,
                        new String[]{"auth-service"},
                        "User Onboarding Enhancement"
                },
                {
                        "Database Connection Pool Exhaustion",
                        "Reporting service exhausting database connection pool during peak hours. Queries taking longer than expected.",
                        Severity.MEDIUM,
                        IncidentStatus.OPEN,
                        new String[]{"reporting-service"},
                        "Admin Portal Revamp"
                },
                {
                        "API Rate Limiting Misconfiguration",
                        "Rate limiting incorrectly configured causing legitimate API calls to be blocked. Affecting partner integrations.",
                        Severity.LOW,
                        IncidentStatus.RESOLVED,
                        new String[]{"admin-service"},
                        "Admin Portal Revamp"
                }
        };

        Map<String, Microservice> msMap = new HashMap<>();
        for (Microservice ms : microservices) {
            msMap.put(ms.getName(), ms);
        }

        Map<String, Feature> featureMap = new HashMap<>();
        for (Feature f : features) {
            featureMap.put(f.getName(), f);
        }

        List<Incident> incidents = new ArrayList<>();

        for (Object[] data : incidentData) {
            String title = (String) data[0];
            String description = (String) data[1];
            Severity severity = (Severity) data[2];
            IncidentStatus status = (IncidentStatus) data[3];
            String[] msNames = (String[]) data[4];
            String featureName = (String) data[5];

            Set<Microservice> incidentMicroservices = new HashSet<>();
            for (String msName : msNames) {
                Microservice ms = msMap.get(msName);
                if (ms != null) {
                    incidentMicroservices.add(ms);
                }
            }

            Incident incident = Incident.builder()
                    .title(title)
                    .description(description)
                    .severity(severity)
                    .status(status)
                    .microservices(incidentMicroservices)
                    .mainFeature(featureMap.get(featureName))
                    .createdBy("admin")
                    .build();

            if (status == IncidentStatus.RESOLVED) {
                incident.setResolvedAt(LocalDateTime.now().minusDays(random.nextInt(5)));
            }

            setRandomCreatedAt(incident, 15);
            incidents.add(incidentRepository.save(incident));
        }

        log.info("Created {} incidents", incidents.size());
        return incidents;
    }

    private List<Hotfix> createHotfixes(List<Feature> features, List<Microservice> microservices) {
        Object[][] hotfixData = {
                {
                        "Payment Timeout Fix",
                        "Increased gateway timeout from 10s to 30s and added retry logic with exponential backoff for transient failures.",
                        "v2.8.1-hotfix",
                        HotfixStatus.DEPLOYED,
                        new String[]{"payment-service"},
                        "Payment Gateway Upgrade"
                },
                {
                        "Wallet Balance Cache Invalidation",
                        "Fixed cache invalidation logic to ensure balance updates are immediately reflected. Added distributed lock for concurrent updates.",
                        "v3.0.2-hotfix",
                        HotfixStatus.DEPLOYED,
                        new String[]{"wallet-service"},
                        "Wallet Performance Optimization"
                },
                {
                        "APNs Certificate Renewal",
                        "Renewed Apple Push Notification service certificate and implemented automated certificate rotation monitoring.",
                        "v1.9.4-hotfix",
                        HotfixStatus.DEPLOYED,
                        new String[]{"notification-service"},
                        "User Onboarding Enhancement"
                },
                {
                        "Fraud Model Rollback",
                        "Rolled back to previous ML model version (v1.1.5) while investigating false positive issues in new model.",
                        "v1.2.1-hotfix",
                        HotfixStatus.IN_PROGRESS,
                        new String[]{"fraud-engine"},
                        "Fraud Detection Improvement"
                },
                {
                        "Auth Memory Leak Patch",
                        "Fixed JWT token cache eviction policy. Implemented proper cleanup of expired tokens and added memory monitoring.",
                        "v1.5.3-hotfix",
                        HotfixStatus.IN_PROGRESS,
                        new String[]{"auth-service"},
                        "User Onboarding Enhancement"
                },
                {
                        "KYC Queue Optimization",
                        "Optimized document processing queue with parallel processing and increased worker threads from 4 to 16.",
                        "v2.1.1-hotfix",
                        HotfixStatus.PLANNED,
                        new String[]{"kyc-service"},
                        "KYC Automation Release"
                },
                {
                        "Database Index Optimization",
                        "Added missing indexes on frequently queried columns. Reduced average query time from 500ms to 50ms.",
                        "v2.0.6-hotfix",
                        HotfixStatus.DEPLOYED,
                        new String[]{"reporting-service"},
                        "Admin Portal Revamp"
                }
        };

        Map<String, Microservice> msMap = new HashMap<>();
        for (Microservice ms : microservices) {
            msMap.put(ms.getName(), ms);
        }

        Map<String, Feature> featureMap = new HashMap<>();
        for (Feature f : features) {
            featureMap.put(f.getName(), f);
        }

        List<Hotfix> hotfixes = new ArrayList<>();

        for (Object[] data : hotfixData) {
            String title = (String) data[0];
            String description = (String) data[1];
            String releaseVersion = (String) data[2];
            HotfixStatus status = (HotfixStatus) data[3];
            String[] msNames = (String[]) data[4];
            String featureName = (String) data[5];

            Set<Microservice> hotfixMicroservices = new HashSet<>();
            for (String msName : msNames) {
                Microservice ms = msMap.get(msName);
                if (ms != null) {
                    hotfixMicroservices.add(ms);
                }
            }

            Hotfix hotfix = Hotfix.builder()
                    .title(title)
                    .description(description)
                    .releaseVersion(releaseVersion)
                    .status(status)
                    .microservices(hotfixMicroservices)
                    .mainFeature(featureMap.get(featureName))
                    .createdBy("admin")
                    .build();

            if (status == HotfixStatus.DEPLOYED) {
                hotfix.setDeployedAt(LocalDateTime.now().minusDays(random.nextInt(7)));
            }

            setRandomCreatedAt(hotfix, 10);
            hotfixes.add(hotfixRepository.save(hotfix));
        }

        log.info("Created {} hotfixes", hotfixes.size());
        return hotfixes;
    }

    private List<Issue> createIssues(List<Feature> features) {
        Object[][] issueData = {
                {
                        "Improve Error Messages for Failed Transactions",
                        "Current error messages are too technical. Need user-friendly messages explaining why transaction failed and suggested actions.",
                        IssuePriority.MEDIUM,
                        IssueStatus.IN_PROGRESS,
                        "Payment Gateway Upgrade"
                },
                {
                        "Add Biometric Authentication Support",
                        "Implement fingerprint and face recognition authentication for mobile app users to improve security and UX.",
                        IssuePriority.HIGH,
                        IssueStatus.OPEN,
                        "User Onboarding Enhancement"
                },
                {
                        "Optimize KYC Document Upload",
                        "Document upload is slow on poor network connections. Implement chunked upload with resume capability.",
                        IssuePriority.MEDIUM,
                        IssueStatus.IN_PROGRESS,
                        "KYC Automation Release"
                },
                {
                        "Add Transaction Export Feature",
                        "Users requesting ability to export transaction history to CSV/PDF for accounting purposes.",
                        IssuePriority.LOW,
                        IssueStatus.OPEN,
                        "Wallet Performance Optimization"
                },
                {
                        "Implement Real-time Fraud Alerts",
                        "Send immediate push notifications when suspicious activity is detected on user account.",
                        IssuePriority.HIGH,
                        IssueStatus.IN_PROGRESS,
                        "Fraud Detection Improvement"
                },
                {
                        "Add Dark Mode Support",
                        "Implement dark mode theme for admin portal to reduce eye strain during night shifts.",
                        IssuePriority.LOW,
                        IssueStatus.OPEN,
                        "Admin Portal Revamp"
                },
                {
                        "Improve Search Performance",
                        "Search functionality in admin portal is slow for large datasets. Need to implement Elasticsearch.",
                        IssuePriority.MEDIUM,
                        IssueStatus.IN_PROGRESS,
                        "Admin Portal Revamp"
                },
                {
                        "Add Multi-language Support",
                        "Support for multiple languages (Myanmar, English, Chinese) in user-facing applications.",
                        IssuePriority.HIGH,
                        IssueStatus.OPEN,
                        "User Onboarding Enhancement"
                },
                {
                        "Implement Scheduled Reports",
                        "Allow admins to schedule automated reports to be generated and emailed at specified intervals.",
                        IssuePriority.MEDIUM,
                        IssueStatus.RESOLVED,
                        "Admin Portal Revamp"
                },
                {
                        "Add Two-Factor Authentication",
                        "Implement 2FA using TOTP for admin users to enhance security of administrative access.",
                        IssuePriority.URGENT,
                        IssueStatus.IN_PROGRESS,
                        "User Onboarding Enhancement"
                },
                {
                        "Optimize Mobile App Battery Usage",
                        "Background sync consuming too much battery. Need to optimize sync intervals and use WorkManager.",
                        IssuePriority.MEDIUM,
                        IssueStatus.OPEN,
                        "User Onboarding Enhancement"
                },
                {
                        "Add Webhook Notifications",
                        "Implement webhook support for partners to receive real-time transaction notifications.",
                        IssuePriority.HIGH,
                        IssueStatus.IN_PROGRESS,
                        "Payment Gateway Upgrade"
                }
        };

        Map<String, Feature> featureMap = new HashMap<>();
        for (Feature f : features) {
            featureMap.put(f.getName(), f);
        }

        List<Issue> issues = new ArrayList<>();

        for (Object[] data : issueData) {
            String title = (String) data[0];
            String description = (String) data[1];
            IssuePriority priority = (IssuePriority) data[2];
            IssueStatus status = (IssueStatus) data[3];
            String featureName = (String) data[4];

            Issue issue = Issue.builder()
                    .title(title)
                    .description(description)
                    .priority(priority)
                    .status(status)
                    .mainFeature(featureMap.get(featureName))
                    .createdBy("admin")
                    .build();

            if (status == IssueStatus.RESOLVED) {
                issue.setResolvedAt(LocalDateTime.now().minusDays(random.nextInt(10)));
            }

            setRandomCreatedAt(issue, 20);
            issues.add(issueRepository.save(issue));
        }

        log.info("Created {} issues", issues.size());
        return issues;
    }
}
