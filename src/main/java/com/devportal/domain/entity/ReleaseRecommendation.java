package com.devportal.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "release_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "release_id", nullable = false)
    private Release release;

    @Column(name = "recommended_entity_type", nullable = false, length = 50)
    private String recommendedEntityType;

    @Column(name = "recommended_entity_id", nullable = false)
    private UUID recommendedEntityId;

    @Column(name = "recommendation_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal recommendationScore;

    @Column(name = "recommendation_reason", length = 255)
    private String recommendationReason;

    @Column(name = "is_accepted")
    @Builder.Default
    private Boolean isAccepted = false;

    @Column(name = "is_dismissed")
    @Builder.Default
    private Boolean isDismissed = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
