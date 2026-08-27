package com.example.backend.assessment.repository;

import com.example.backend.assessment.entity.AssessmentWorkspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssessmentWorkspaceRepository extends JpaRepository<AssessmentWorkspace, UUID> {
    Optional<AssessmentWorkspace> findByAssessmentId(UUID assessmentId);
}
