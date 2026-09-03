package com.example.backend.pipeline.application;

import com.example.backend.pipeline.orchestration.AssessmentProcessingOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentProcessingLauncher {

    private final AssessmentProcessingOrchestrator orchestrator;

    @Async
    public CompletableFuture<Void> launchProcessing(UUID assessmentId, String repoUrl, String branch, String backendRootDir) {
        log.info("Launching asynchronous pipeline for assessment: {}", assessmentId);
        try {
            orchestrator.executePipeline(assessmentId, repoUrl, branch, backendRootDir);
            return CompletableFuture.completedFuture(null);
        } catch (Exception ex) {
            log.error("Asynchronous pipeline execution failed for assessment {}: {}", assessmentId, ex.getMessage(), ex);
            return CompletableFuture.failedFuture(ex);
        }
    }
}
