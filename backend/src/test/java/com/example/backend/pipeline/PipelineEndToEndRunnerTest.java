package com.example.backend.pipeline;

import com.example.backend.pipeline.analysis.AstAnalysisService;
import com.example.backend.pipeline.docker.DockerCommandExecutor;
import com.example.backend.pipeline.docker.DockerValidationService;
import com.example.backend.pipeline.feature.FeaturePromptBuilder;
import com.example.backend.pipeline.feature.FeatureSpecificationService;
import com.example.backend.pipeline.feature.client.MistralAiClient;
import com.example.backend.pipeline.feature.config.MistralAiConfig;
import com.example.backend.pipeline.git.GitCloningService;
import com.example.backend.pipeline.orchestration.AssessmentProcessingOrchestrator;
import com.example.backend.pipeline.orchestration.dto.PipelineExecutionResult;
import com.example.backend.pipeline.testcase.TestCaseGenerationService;
import com.example.backend.pipeline.testcase.TestCasePromptBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PipelineEndToEndRunnerTest {

    @Test
    @DisplayName("Run complete 5-phase pipeline on real GitHub repository and store visible inspection outcomes")
    void runCompletePipeline() {
        String repoUrl = System.getProperty("repoUrl", "https://github.com/Yasvanth0508/Student-Management-System.git");
        String branch = System.getProperty("branch", "main");
        String backendDir = System.getProperty("backendDir", "github_practice");

        UUID assessmentId = UUID.randomUUID();

        // Instantiate live Spring services
        GitCloningService gitService = new GitCloningService(null, null);
        DockerCommandExecutor dockerExecutor = new DockerCommandExecutor();
        DockerValidationService dockerService = new DockerValidationService(dockerExecutor);
        AstAnalysisService astService = new AstAnalysisService(null, null);

        MistralAiConfig config = new MistralAiConfig();
        MistralAiClient client = new MistralAiClient(config);

        FeaturePromptBuilder featurePromptBuilder = new FeaturePromptBuilder();
        FeatureSpecificationService featureService = new FeatureSpecificationService(
                client, config, featurePromptBuilder, null, null
        );

        TestCasePromptBuilder testCasePromptBuilder = new TestCasePromptBuilder();
        TestCaseGenerationService testCaseService = new TestCaseGenerationService(
                client, config, testCasePromptBuilder, null, null
        );

        AssessmentProcessingOrchestrator orchestrator = new AssessmentProcessingOrchestrator(
                gitService, dockerService, astService, featureService, testCaseService, null
        );

        System.out.println("===================================================================");
        System.out.println("TRIGGERING LIVE END-TO-END PIPELINE");
        System.out.println("Assessment ID:   " + assessmentId);
        System.out.println("Repository URL:  " + repoUrl);
        System.out.println("Branch:          " + branch);
        System.out.println("Backend Root:    " + backendDir);
        System.out.println("===================================================================");

        // Execute live pipeline
        PipelineExecutionResult result = orchestrator.executePipeline(assessmentId, repoUrl, branch, backendDir);

        System.out.println("===================================================================");
        System.out.println("END-TO-END PIPELINE RESULT:");
        System.out.println("Overall Success: " + result.isSuccess());
        System.out.println("Total Time:      " + result.getTotalDurationMs() + " ms");
        System.out.println("Inspection Dir:  " + result.getInspectionFolderPath());
        System.out.println("===================================================================");

        assertTrue(result.isSuccess(), "Pipeline must succeed: " + result.getErrorMessage());

        // Verify generated inspection files on disk
        Path inspPath = Paths.get(result.getInspectionFolderPath());
        assertTrue(Files.exists(inspPath), "Inspection directory must exist on disk");

        System.out.println("VISIBLE ARTIFACTS GENERATED ON DISK (" + inspPath.toAbsolutePath() + "):");
        try (Stream<Path> stream = Files.list(inspPath)) {
            stream.sorted().forEach(file -> {
                long size = file.toFile().length();
                System.out.println("  * " + file.getFileName() + " (" + size + " bytes)");
            });
        } catch (Exception ignored) {
        }
        System.out.println("===================================================================");
    }
}
