package com.devportal.domain.entity;

import com.devportal.domain.enums.HotfixStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "hotfixes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotfix extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "release_version", length = 50)
    private String releaseVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private HotfixStatus status = HotfixStatus.PLANNED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_feature_id", nullable = false)
    private Feature mainFeature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "deployed_at")
    private LocalDateTime deployedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hotfix_microservices",
            joinColumns = @JoinColumn(name = "hotfix_id"),
            inverseJoinColumns = @JoinColumn(name = "microservice_id")
    )
    @Builder.Default
    private Set<Microservice> microservices = new HashSet<>();
}
