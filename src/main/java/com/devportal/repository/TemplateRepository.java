package com.devportal.repository;

import com.devportal.domain.entity.Template;
import com.devportal.domain.enums.TemplateEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateRepository extends JpaRepository<Template, UUID> {

    List<Template> findByEntityTypeAndIsActiveTrue(TemplateEntityType entityType);

    List<Template> findByIsActiveTrue();

    Optional<Template> findByEntityTypeAndIsDefaultTrue(TemplateEntityType entityType);

    List<Template> findByCreatedById(UUID userId);
}
