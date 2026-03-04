package com.devportal.domain.entity;

import com.devportal.domain.enums.IssueCategory;
import com.devportal.domain.enums.IssuePriority;
import com.devportal.domain.enums.IssueStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "issues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Issue extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private IssuePriority priority = IssuePriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private IssueStatus status = IssueStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private IssueCategory category = IssueCategory.OTHER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_feature_id", nullable = false)
    private Feature mainFeature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "result_comment", columnDefinition = "TEXT")
    private String resultComment;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
