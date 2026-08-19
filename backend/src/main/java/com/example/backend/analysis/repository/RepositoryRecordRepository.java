package com.example.backend.analysis.repository;

import com.example.backend.analysis.entity.RepositoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryRecordRepository extends JpaRepository<RepositoryRecord, UUID> {
    Optional<RepositoryRecord> findByAssessmentId(UUID assessmentId);
}
