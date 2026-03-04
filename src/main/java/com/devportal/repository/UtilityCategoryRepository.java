package com.devportal.repository;

import com.devportal.domain.entity.UtilityCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilityCategoryRepository extends JpaRepository<UtilityCategory, UUID> {

    Optional<UtilityCategory> findByName(String name);

    List<UtilityCategory> findByParentIsNullOrderBySortOrder();

    List<UtilityCategory> findByParentIdOrderBySortOrder(UUID parentId);

    @Query("SELECT c FROM UtilityCategory c ORDER BY c.sortOrder, c.name")
    List<UtilityCategory> findAllOrderBySortOrder();
}
