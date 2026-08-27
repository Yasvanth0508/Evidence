package com.example.backend.assessment.repository;

import com.example.backend.assessment.entity.RepositoryAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryAnalysisRepository extends JpaRepository<RepositoryAnalysis, UUID> {
    Optional<RepositoryAnalysis> findByAssessmentId(UUID assessmentId);
}
