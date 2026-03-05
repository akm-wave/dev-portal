package com.devportal.domain.entity;

import com.devportal.domain.enums.ChecklistPriority;
import com.devportal.domain.enums.ChecklistStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "checklists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Checklist extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ChecklistStatus status = ChecklistStatus.PLANNED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private ChecklistPriority priority = ChecklistPriority.MEDIUM;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "weight")
    @Builder.Default
    private Integer weight = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToMany(mappedBy = "checklists")
    @Builder.Default
    private Set<Microservice> microservices = new HashSet<>();
}
