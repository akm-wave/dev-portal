package com.devportal.domain.entity;

import com.devportal.domain.enums.ChecklistStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feature_checkpoints", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"feature_id", "checklist_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureCheckpoint extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private Feature feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ChecklistStatus status = ChecklistStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "mongo_file_id", length = 100)
    private String mongoFileId;

    @Column(name = "attachment_filename", length = 255)
    private String attachmentFilename;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
