package com.example.backend.assessment.repository;

import com.example.backend.assessment.entity.FeatureSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureSpecificationRepository extends JpaRepository<FeatureSpecification, UUID> {
    Optional<FeatureSpecification> findByAssessmentId(UUID assessmentId);
}
