package com.devportal.repository;

import com.devportal.domain.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByName(String name);

    List<Tag> findByNameContainingIgnoreCase(String name);

    @Query("SELECT t FROM Tag t JOIN t.utilities u WHERE u.id = :utilityId")
    List<Tag> findByUtilityId(@Param("utilityId") UUID utilityId);

    @Query("SELECT t FROM Tag t ORDER BY t.name")
    List<Tag> findAllOrderByName();
}
