package com.example.backend.evaluation.repository;

import com.example.backend.evaluation.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    List<Submission> findAllByAssessmentId(UUID assessmentId);
    Optional<Submission> findTopByAssessmentIdOrderBySubmittedAtDesc(UUID assessmentId);
}
