package com.example.backend.workspace.repository;

import com.example.backend.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    List<Workspace> findAllByRecruiterId(UUID recruiterId);
    Optional<Workspace> findByIdAndRecruiterId(UUID id, UUID recruiterId);
    long countByRecruiterId(UUID recruiterId);
}
