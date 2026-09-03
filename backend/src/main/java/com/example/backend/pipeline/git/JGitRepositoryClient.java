package com.example.backend.pipeline.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class JGitRepositoryClient implements GitRepositoryClient {

    private static final Logger log = LoggerFactory.getLogger(JGitRepositoryClient.class);

    @Override
    public boolean cloneRepository(String repoUrl, String branch, Path targetDir) {
        boolean cloned = cloneWithNativeGit(repoUrl, branch, targetDir);
        if (!cloned) {
            log.info("Native git clone was not successful, trying Eclipse JGit clone...");
            cloned = cloneWithJGit(repoUrl, branch, targetDir);
        }
        return cloned;
    }

    private boolean cloneWithNativeGit(String repoUrl, String branch, Path targetDir) {
        try {
            List<String> command = List.of(
                    "git", "clone",
                    "--branch", branch,
                    "--depth", "1",
                    repoUrl,
                    targetDir.toAbsolutePath().toString()
            );

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (finished && process.exitValue() == 0) {
                log.info("Native Git clone completed successfully.");
                return true;
            } else {
                log.warn("Native Git clone returned exit code {}: {}", process.exitValue(), output);
                return false;
            }
        } catch (Exception e) {
            log.warn("Native Git clone exception: {}", e.getMessage());
            return false;
        }
    }

    private boolean cloneWithJGit(String repoUrl, String branch, Path targetDir) {
        try (Git git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(targetDir.toFile())
                .setBranch(branch)
                .call()) {
            return true;
        } catch (Exception ex) {
            log.warn("JGit clone exception: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public String extractCommitHash(Path targetDir) {
        // Try native git rev-parse HEAD
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "rev-parse", "HEAD");
            pb.directory(targetDir.toFile());
            Process p = pb.start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = r.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    return line.trim();
                }
            }
        } catch (Exception ignored) {
        }

        // Try JGit repo resolve
        try (Git git = Git.open(targetDir.toFile())) {
            Repository repo = git.getRepository();
            ObjectId head = repo.resolve("HEAD");
            if (head != null) {
                return head.getName();
            }
        } catch (Exception ignored) {
        }

        return "UNKNOWN";
    }
}
