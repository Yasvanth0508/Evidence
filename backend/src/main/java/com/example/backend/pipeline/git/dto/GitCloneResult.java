package com.example.backend.pipeline.git.dto;

import java.util.List;

public class GitCloneResult {

    private boolean success;
    private String localRepositoryPath;
    private String branchName;
    private String commitHash;
    private int totalFiles;
    private long totalSizeBytes;
    private List<String> topLevelFiles;
    private String errorMessage;

    public GitCloneResult() {
    }

    public GitCloneResult(boolean success, String localRepositoryPath, String branchName,
                          String commitHash, int totalFiles, long totalSizeBytes,
                          List<String> topLevelFiles, String errorMessage) {
        this.success = success;
        this.localRepositoryPath = localRepositoryPath;
        this.branchName = branchName;
        this.commitHash = commitHash;
        this.totalFiles = totalFiles;
        this.totalSizeBytes = totalSizeBytes;
        this.topLevelFiles = topLevelFiles;
        this.errorMessage = errorMessage;
    }

    public static GitCloneResult ok(String path, String branch, String commit, int files, long size, List<String> topFiles) {
        return new GitCloneResult(true, path, branch, commit, files, size, topFiles, null);
    }

    public static GitCloneResult fail(String errorMessage) {
        return new GitCloneResult(false, null, null, null, 0, 0L, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getLocalRepositoryPath() {
        return localRepositoryPath;
    }

    public void setLocalRepositoryPath(String localRepositoryPath) {
        this.localRepositoryPath = localRepositoryPath;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public long getTotalSizeBytes() {
        return totalSizeBytes;
    }

    public void setTotalSizeBytes(long totalSizeBytes) {
        this.totalSizeBytes = totalSizeBytes;
    }

    public List<String> getTopLevelFiles() {
        return topLevelFiles;
    }

    public void setTopLevelFiles(List<String> topLevelFiles) {
        this.topLevelFiles = topLevelFiles;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
