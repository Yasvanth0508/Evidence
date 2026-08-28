package com.example.backend.assessment.repository;

import com.example.backend.assessment.entity.EvaluationReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EvaluationReportRepository extends JpaRepository<EvaluationReport, UUID> {
    Optional<EvaluationReport> findBySubmissionId(UUID submissionId);
}
