package com.example.backend.assessment.repository;

import com.example.backend.assessment.entity.EvaluationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EvaluationReportRepository extends JpaRepository<EvaluationReport, UUID> {
    Optional<EvaluationReport> findBySubmissionId(UUID submissionId);
}
