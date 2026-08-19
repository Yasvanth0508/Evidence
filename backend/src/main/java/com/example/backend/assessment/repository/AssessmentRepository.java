package com.example.backend.assessment.repository;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.common.enums.AssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
    List<Assessment> findAllByWorkspaceId(UUID workspaceId);
    List<Assessment> findAllByCandidateId(UUID candidateId);
    Optional<Assessment> findByIdAndCandidateId(UUID id, UUID candidateId);
    long countByWorkspaceId(UUID workspaceId);
    long countByStatus(AssessmentStatus status);
}
