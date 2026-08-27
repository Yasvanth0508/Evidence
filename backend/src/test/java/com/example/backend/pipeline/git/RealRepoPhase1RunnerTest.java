package com.example.backend.pipeline.git;

import com.example.backend.pipeline.git.dto.GitCloneResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class RealRepoPhase1RunnerTest {

    @Test
    @DisplayName("Phase 1: Clone user's Student-Management-System repository and verify storage")
    void testCloneRealUserRepo() throws IOException {
        GitCloningService gitCloningService = new GitCloningService(null, null);

        // Fixed assessment ID for user inspection
        UUID assessmentId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        String repoUrl = "https://github.com/Yasvanth0508/Student-Management-System.git";
        String branch = "main";
        String backendSubdir = "github_practice";

        System.out.println("==================================================");
        System.out.println("STARTING PHASE 1 ON REAL USER REPOSITORY");
        System.out.println("Repository: " + repoUrl);
        System.out.println("Branch:     " + branch);
        System.out.println("Subfolder:  " + backendSubdir);
        System.out.println("==================================================");

        GitCloneResult result = gitCloningService.cloneRepository(assessmentId, repoUrl, branch);

        System.out.println("==================================================");
        System.out.println("PHASE 1 REAL CLONE RESULT:");
        System.out.println("Success:      " + result.isSuccess());
        System.out.println("Cloned Path:  " + result.getLocalRepositoryPath());
        System.out.println("Branch:       " + result.getBranchName());
        System.out.println("Commit SHA:   " + result.getCommitHash());
        System.out.println("Total Files:  " + result.getTotalFiles());
        System.out.println("Total Size:   " + result.getTotalSizeBytes() + " bytes");
        System.out.println("Top Files:    " + result.getTopLevelFiles());

        assertTrue(result.isSuccess(), "Clone failed: " + result.getErrorMessage());

        Path clonedPath = Paths.get(result.getLocalRepositoryPath());
        assertTrue(Files.exists(clonedPath), "Cloned directory must exist on disk");

        Path subfolderPath = clonedPath.resolve(backendSubdir);
        System.out.println("Subdirectory exists: " + Files.exists(subfolderPath));
        if (Files.exists(subfolderPath)) {
            System.out.println("--- Contents of " + backendSubdir + " ---");
            try (Stream<Path> stream = Files.walk(subfolderPath, 3)) {
                List<String> items = stream.map(p -> subfolderPath.relativize(p).toString())
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                items.forEach(item -> System.out.println("  - " + item));
            }
        }

        System.out.println("==================================================");
    }
}
