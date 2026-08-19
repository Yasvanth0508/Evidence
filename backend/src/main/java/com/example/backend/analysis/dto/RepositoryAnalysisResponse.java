package com.example.backend.analysis.dto;

public class RepositoryAnalysisResponse {

    private String analysisStatus;
    private ProjectStructureDto projectStructure;
    private SourceCodeStructureDto sourceCodeStructure;
    private ContentDetailsDto contentDetails;

    public RepositoryAnalysisResponse() {
    }

    public RepositoryAnalysisResponse(String analysisStatus,
                                      ProjectStructureDto projectStructure,
                                      SourceCodeStructureDto sourceCodeStructure,
                                      ContentDetailsDto contentDetails) {
        this.analysisStatus = analysisStatus;
        this.projectStructure = projectStructure;
        this.sourceCodeStructure = sourceCodeStructure;
        this.contentDetails = contentDetails;
    }

    public String getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(String analysisStatus) {
        this.analysisStatus = analysisStatus;
    }

    public ProjectStructureDto getProjectStructure() {
        return projectStructure;
    }

    public void setProjectStructure(ProjectStructureDto projectStructure) {
        this.projectStructure = projectStructure;
    }

    public SourceCodeStructureDto getSourceCodeStructure() {
        return sourceCodeStructure;
    }

    public void setSourceCodeStructure(SourceCodeStructureDto sourceCodeStructure) {
        this.sourceCodeStructure = sourceCodeStructure;
    }

    public ContentDetailsDto getContentDetails() {
        return contentDetails;
    }

    public void setContentDetails(ContentDetailsDto contentDetails) {
        this.contentDetails = contentDetails;
    }
}
