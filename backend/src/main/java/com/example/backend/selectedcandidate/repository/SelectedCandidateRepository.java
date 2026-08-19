package com.example.backend.selectedcandidate.repository;

import com.example.backend.selectedcandidate.entity.SelectedCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SelectedCandidateRepository extends JpaRepository<SelectedCandidate, UUID> {
    List<SelectedCandidate> findAllByRecruiterId(UUID recruiterId);
    List<SelectedCandidate> findAllByWorkspaceId(UUID workspaceId);
    List<SelectedCandidate> findAllByRecruiterIdAndWorkspaceId(UUID recruiterId, UUID workspaceId);
    Optional<SelectedCandidate> findByWorkspaceIdAndCandidateId(UUID workspaceId, UUID candidateId);
    boolean existsByWorkspaceIdAndCandidateId(UUID workspaceId, UUID candidateId);
}
