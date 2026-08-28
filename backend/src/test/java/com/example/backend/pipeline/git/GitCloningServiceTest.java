package com.example.backend.pipeline.git;

import com.example.backend.pipeline.git.dto.GitCloneResult;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GitCloningServiceTest {

    @Test
    @DisplayName("Phase 1: Clone local Git repository and extract files & metadata")
    void testPhase1GitClone(@TempDir Path tempRepoDir) throws Exception {
        // 1. Initialize a real local Git repository to serve as the remote origin
        File repoFolder = tempRepoDir.toFile();
        Git localGit = Git.init().setDirectory(repoFolder).call();

        // Create sample project files
        Files.writeString(tempRepoDir.resolve("pom.xml"), "<project><name>sample-app</name></project>");
        Files.writeString(tempRepoDir.resolve("README.md"), "# Sample Candidate App");
        Files.createDirectories(tempRepoDir.resolve("src/main/java"));
        Files.writeString(tempRepoDir.resolve("src/main/java/Main.java"), "public class Main { public static void main(String[] args) {} }");

        // Commit files
        localGit.add().addFilepattern(".").call();
        localGit.commit().setMessage("Initial project commit").call();
        String branch = localGit.getRepository().getBranch();
        localGit.close();

        // 2. Instantiate GitCloningService (with mock repos for isolated unit test)
        GitCloningService gitCloningService = new GitCloningService(null, null);

        UUID assessmentId = UUID.randomUUID();
        String fileUri = tempRepoDir.toUri().toString();

        // 3. Execute Phase 1
        GitCloneResult result = gitCloningService.cloneRepository(assessmentId, fileUri, branch);

        // 4. Validate Phase 1 Outcome
        System.out.println("==================================================");
        System.out.println("PHASE 1 EXECUTION OUTCOME:");
        System.out.println("Status Success: " + result.isSuccess());
        System.out.println("Cloned Path:    " + result.getLocalRepositoryPath());
        System.out.println("Branch:         " + result.getBranchName());
        System.out.println("Commit Hash:    " + result.getCommitHash());
        System.out.println("Total Files:    " + result.getTotalFiles());
        System.out.println("Total Size:     " + result.getTotalSizeBytes() + " bytes");
        System.out.println("Top-level Files:" + result.getTopLevelFiles());
        System.out.println("==================================================");

        assertTrue(result.isSuccess(), "Git clone should succeed: " + result.getErrorMessage());
        assertNotNull(result.getLocalRepositoryPath());
        assertNotNull(result.getCommitHash());
        assertTrue(result.getTotalFiles() >= 3);
        assertTrue(result.getTopLevelFiles().contains("pom.xml"));
        assertTrue(result.getTopLevelFiles().contains("README.md"));

        // Cleanup created storage
        gitCloningService.deleteDirectoryRecursively(gitCloningService.resolveAssessmentOriginalPath(assessmentId));
    }
}
