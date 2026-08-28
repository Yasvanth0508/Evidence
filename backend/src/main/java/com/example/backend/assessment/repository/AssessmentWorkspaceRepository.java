package com.example.backend.assessment.repository;

import com.example.backend.assessment.entity.AssessmentWorkspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssessmentWorkspaceRepository extends JpaRepository<AssessmentWorkspace, UUID> {
    Optional<AssessmentWorkspace> findByAssessmentId(UUID assessmentId);
}
