package com.example.backend.execution.repository;

import com.example.backend.execution.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExecutionRepository extends JpaRepository<Execution, UUID> {
    List<Execution> findAllByAssessmentId(UUID assessmentId);
    Optional<Execution> findTopByAssessmentIdOrderByCreatedAtDesc(UUID assessmentId);
}
