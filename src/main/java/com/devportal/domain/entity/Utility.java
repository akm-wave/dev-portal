package com.devportal.domain.entity;

import com.devportal.domain.enums.UtilityType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "utilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utility {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 300)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private UtilityType type = UtilityType.OTHERS;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    private String version;

    @Column(name = "current_version")
    @Builder.Default
    private Integer currentVersion = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private UtilityCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "utility", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UtilityAttachment> attachments = new HashSet<>();

    @OneToMany(mappedBy = "utility", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UtilityVersion> versions = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "utility_tags",
        joinColumns = @JoinColumn(name = "utility_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
