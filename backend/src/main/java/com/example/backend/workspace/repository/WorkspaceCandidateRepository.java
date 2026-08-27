package com.example.backend.workspace.repository;

import com.example.backend.workspace.entity.WorkspaceCandidate;
import com.example.backend.workspace.entity.WorkspaceCandidateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceCandidateRepository extends JpaRepository<WorkspaceCandidate, WorkspaceCandidateId> {

    @Query("SELECT wc FROM WorkspaceCandidate wc WHERE wc.workspace.id = :workspaceId")
    List<WorkspaceCandidate> findAllByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    @Query("SELECT wc FROM WorkspaceCandidate wc WHERE wc.workspace.id = :workspaceId AND wc.candidate.id = :candidateId")
    Optional<WorkspaceCandidate> findByWorkspaceIdAndCandidateId(@Param("workspaceId") UUID workspaceId, @Param("candidateId") UUID candidateId);

    @Query("SELECT CASE WHEN COUNT(wc) > 0 THEN true ELSE false END FROM WorkspaceCandidate wc WHERE wc.workspace.id = :workspaceId AND wc.candidate.id = :candidateId")
    boolean existsByWorkspaceIdAndCandidateId(@Param("workspaceId") UUID workspaceId, @Param("candidateId") UUID candidateId);

    @Query("SELECT wc FROM WorkspaceCandidate wc WHERE wc.candidate.id = :candidateId")
    List<WorkspaceCandidate> findAllByCandidateId(@Param("candidateId") UUID candidateId);

    @Query("SELECT COUNT(DISTINCT wc.candidate.id) FROM WorkspaceCandidate wc WHERE wc.workspace.recruiter.id = :recruiterId")
    long countDistinctCandidatesByRecruiterId(@Param("recruiterId") UUID recruiterId);
}
