package com.devportal.domain.entity;

import com.devportal.domain.enums.IncidentStatus;
import com.devportal.domain.enums.Severity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Severity severity = Severity.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private IncidentStatus status = IncidentStatus.PLANNED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_feature_id", nullable = false)
    private Feature mainFeature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "incident_microservices",
            joinColumns = @JoinColumn(name = "incident_id"),
            inverseJoinColumns = @JoinColumn(name = "microservice_id")
    )
    @Builder.Default
    private Set<Microservice> microservices = new HashSet<>();
}
