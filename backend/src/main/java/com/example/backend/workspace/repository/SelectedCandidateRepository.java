package com.example.backend.workspace.repository;

import com.example.backend.workspace.entity.SelectedCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SelectedCandidateRepository extends JpaRepository<SelectedCandidate, UUID> {

    List<SelectedCandidate> findAllByRecruiterIdOrderBySelectedAtDesc(UUID recruiterId);

    List<SelectedCandidate> findAllByRecruiterIdAndWorkspaceIdOrderBySelectedAtDesc(UUID recruiterId, UUID workspaceId);

    Optional<SelectedCandidate> findByWorkspaceIdAndCandidateId(UUID workspaceId, UUID candidateId);

    Optional<SelectedCandidate> findByRecruiterIdAndWorkspaceIdAndCandidateId(UUID recruiterId, UUID workspaceId, UUID candidateId);

    @Modifying
    @Transactional
    void deleteByRecruiterIdAndWorkspaceIdAndCandidateId(UUID recruiterId, UUID workspaceId, UUID candidateId);

    long countByRecruiterId(UUID recruiterId);
}
