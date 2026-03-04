package com.devportal.domain.entity;

import com.devportal.domain.enums.MicroserviceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "microservices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Microservice extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MicroserviceStatus status = MicroserviceStatus.NOT_STARTED;

    @Column(name = "high_risk")
    @Builder.Default
    private Boolean highRisk = false;

    @Column(name = "technical_debt_score")
    @Builder.Default
    private Integer technicalDebtScore = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "microservice_checklists",
            joinColumns = @JoinColumn(name = "microservice_id"),
            inverseJoinColumns = @JoinColumn(name = "checklist_id")
    )
    @Builder.Default
    private Set<Checklist> checklists = new HashSet<>();

    @ManyToMany(mappedBy = "microservices")
    @Builder.Default
    private Set<Feature> features = new HashSet<>();

    @Column(name = "gitlab_url", length = 500)
    private String gitlabUrl;
}
