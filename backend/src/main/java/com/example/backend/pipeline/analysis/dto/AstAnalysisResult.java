package com.example.backend.pipeline.analysis.dto;

public class AstAnalysisResult {
    private boolean success;
    private ProjectStructureDto projectStructure;
    private SourceCodeStructureDto sourceCodeStructure;
    private ContentDetailsDto contentDetails;
    private String projectStructureJson;
    private String sourceCodeStructureJson;
    private String contentDetailsJson;
    private String errorMessage;

    public AstAnalysisResult() {}

    public static AstAnalysisResult ok(ProjectStructureDto ps, SourceCodeStructureDto sc, ContentDetailsDto cd,
                                       String psJson, String scJson, String cdJson) {
        AstAnalysisResult res = new AstAnalysisResult();
        res.setSuccess(true);
        res.setProjectStructure(ps);
        res.setSourceCodeStructure(sc);
        res.setContentDetails(cd);
        res.setProjectStructureJson(psJson);
        res.setSourceCodeStructureJson(scJson);
        res.setContentDetailsJson(cdJson);
        return res;
    }

    public static AstAnalysisResult fail(String errorMessage) {
        AstAnalysisResult res = new AstAnalysisResult();
        res.setSuccess(false);
        res.setErrorMessage(errorMessage);
        return res;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public ProjectStructureDto getProjectStructure() { return projectStructure; }
    public void setProjectStructure(ProjectStructureDto projectStructure) { this.projectStructure = projectStructure; }

    public SourceCodeStructureDto getSourceCodeStructure() { return sourceCodeStructure; }
    public void setSourceCodeStructure(SourceCodeStructureDto sourceCodeStructure) { this.sourceCodeStructure = sourceCodeStructure; }

    public ContentDetailsDto getContentDetails() { return contentDetails; }
    public void setContentDetails(ContentDetailsDto contentDetails) { this.contentDetails = contentDetails; }

    public String getProjectStructureJson() { return projectStructureJson; }
    public void setProjectStructureJson(String projectStructureJson) { this.projectStructureJson = projectStructureJson; }

    public String getSourceCodeStructureJson() { return sourceCodeStructureJson; }
    public void setSourceCodeStructureJson(String sourceCodeStructureJson) { this.sourceCodeStructureJson = sourceCodeStructureJson; }

    public String getContentDetailsJson() { return contentDetailsJson; }
    public void setContentDetailsJson(String contentDetailsJson) { this.contentDetailsJson = contentDetailsJson; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
