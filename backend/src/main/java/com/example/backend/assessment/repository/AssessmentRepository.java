package com.example.backend.assessment.repository;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.common.enums.AssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
    List<Assessment> findAllByWorkspaceId(UUID workspaceId);
    List<Assessment> findAllByCandidateId(UUID candidateId);
    List<Assessment> findAllByCandidateIdOrderByCreatedAtDesc(UUID candidateId);
    Optional<Assessment> findByIdAndCandidateId(UUID id, UUID candidateId);
    long countByWorkspaceId(UUID workspaceId);
    long countByStatus(AssessmentStatus status);

    @Query("SELECT COUNT(a) FROM Assessment a WHERE a.workspace.recruiter.id = :recruiterId")
    long countByRecruiterId(@Param("recruiterId") UUID recruiterId);

    @Query("SELECT COUNT(a) FROM Assessment a WHERE a.workspace.recruiter.id = :recruiterId AND a.status IN :statuses")
    long countByRecruiterIdAndStatusIn(@Param("recruiterId") UUID recruiterId, @Param("statuses") Collection<AssessmentStatus> statuses);
}
