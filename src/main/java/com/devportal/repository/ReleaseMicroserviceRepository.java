package com.devportal.repository;

import com.devportal.domain.entity.ReleaseMicroservice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReleaseMicroserviceRepository extends JpaRepository<ReleaseMicroservice, UUID> {

    List<ReleaseMicroservice> findByReleaseId(UUID releaseId);

    Optional<ReleaseMicroservice> findByReleaseIdAndMicroserviceId(UUID releaseId, UUID microserviceId);

    void deleteByReleaseIdAndMicroserviceId(UUID releaseId, UUID microserviceId);

    boolean existsByReleaseIdAndMicroserviceId(UUID releaseId, UUID microserviceId);
}
