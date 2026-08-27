package com.example.backend.assessment.repository;

import com.example.backend.assessment.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    Optional<Submission> findByAssessmentId(UUID assessmentId);
}
