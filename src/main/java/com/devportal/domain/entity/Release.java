package com.devportal.domain.entity;

import com.devportal.domain.enums.ReleaseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "releases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Release {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String version;

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ReleaseStatus status = ReleaseStatus.DRAFT;

    @Column(name = "old_build_number", length = 100)
    private String oldBuildNumber;

    @Column(name = "feature_branch", length = 255)
    private String featureBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "release", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ReleaseMicroservice> releaseMicroservices = new HashSet<>();

    @OneToMany(mappedBy = "release", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ReleaseLink> releaseLinks = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addMicroservice(ReleaseMicroservice releaseMicroservice) {
        releaseMicroservices.add(releaseMicroservice);
        releaseMicroservice.setRelease(this);
    }

    public void removeMicroservice(ReleaseMicroservice releaseMicroservice) {
        releaseMicroservices.remove(releaseMicroservice);
        releaseMicroservice.setRelease(null);
    }

    public void addLink(ReleaseLink releaseLink) {
        releaseLinks.add(releaseLink);
        releaseLink.setRelease(this);
    }

    public void removeLink(ReleaseLink releaseLink) {
        releaseLinks.remove(releaseLink);
        releaseLink.setRelease(null);
    }
}
