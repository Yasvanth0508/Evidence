package com.example.backend.workspace.repository;

import com.example.backend.workspace.entity.WorkspaceCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceCandidateRepository extends JpaRepository<WorkspaceCandidate, UUID> {
    List<WorkspaceCandidate> findAllByWorkspaceId(UUID workspaceId);
    Optional<WorkspaceCandidate> findByWorkspaceIdAndCandidateId(UUID workspaceId, UUID candidateId);
    boolean existsByWorkspaceIdAndCandidateId(UUID workspaceId, UUID candidateId);
    List<WorkspaceCandidate> findAllByCandidateId(UUID candidateId);

    @Query("SELECT COUNT(DISTINCT wc.candidate.id) FROM WorkspaceCandidate wc WHERE wc.workspace.recruiter.id = :recruiterId")
    long countDistinctCandidatesByRecruiterId(@Param("recruiterId") UUID recruiterId);
}
