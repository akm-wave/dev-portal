package com.devportal.repository;

import com.devportal.domain.entity.UtilityVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilityVersionRepository extends JpaRepository<UtilityVersion, UUID> {

    List<UtilityVersion> findByUtilityIdOrderByVersionNumberDesc(UUID utilityId);

    Optional<UtilityVersion> findByUtilityIdAndVersionNumber(UUID utilityId, Integer versionNumber);

    Optional<UtilityVersion> findByUtilityIdAndIsCurrent(UUID utilityId, Boolean isCurrent);

    @Query("SELECT MAX(v.versionNumber) FROM UtilityVersion v WHERE v.utility.id = :utilityId")
    Optional<Integer> findMaxVersionNumber(@Param("utilityId") UUID utilityId);

    @Query("SELECT v FROM UtilityVersion v WHERE v.utility.id = :utilityId AND v.isCurrent = true")
    Optional<UtilityVersion> findCurrentVersion(@Param("utilityId") UUID utilityId);
}
