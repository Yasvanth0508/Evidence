package com.example.backend.assessment.repository;

import com.example.backend.assessment.entity.RepositoryAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RepositoryAnalysisRepository extends JpaRepository<RepositoryAnalysis, UUID> {
    Optional<RepositoryAnalysis> findByAssessmentId(UUID assessmentId);
}
