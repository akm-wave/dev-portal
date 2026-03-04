package com.devportal.repository;

import com.devportal.domain.entity.ReleaseLink;
import com.devportal.domain.enums.ReleaseLinkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReleaseLinkRepository extends JpaRepository<ReleaseLink, UUID> {

    List<ReleaseLink> findByReleaseId(UUID releaseId);

    List<ReleaseLink> findByReleaseIdAndEntityType(UUID releaseId, ReleaseLinkType entityType);

    Optional<ReleaseLink> findByReleaseIdAndEntityTypeAndEntityId(UUID releaseId, ReleaseLinkType entityType, UUID entityId);

    void deleteByReleaseIdAndEntityTypeAndEntityId(UUID releaseId, ReleaseLinkType entityType, UUID entityId);

    boolean existsByReleaseIdAndEntityTypeAndEntityId(UUID releaseId, ReleaseLinkType entityType, UUID entityId);
}
