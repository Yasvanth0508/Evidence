package com.example.backend.pipeline.analysis.dto;

import java.util.ArrayList;
import java.util.List;

public class EndpointInfo {
    private String httpMethod;
    private String path;
    private String fullPath;
    private String handlerMethod;
    private String returnType;
    private String requestBodyType;
    private List<ParameterInfo> parameters = new ArrayList<>();

    public EndpointInfo() {}

    public EndpointInfo(String httpMethod, String path, String fullPath, String handlerMethod, String returnType, String requestBodyType) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.fullPath = fullPath;
        this.handlerMethod = handlerMethod;
        this.returnType = returnType;
        this.requestBodyType = requestBodyType;
    }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getFullPath() { return fullPath; }
    public void setFullPath(String fullPath) { this.fullPath = fullPath; }

    public String getHandlerMethod() { return handlerMethod; }
    public void setHandlerMethod(String handlerMethod) { this.handlerMethod = handlerMethod; }

    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }

    public String getRequestBodyType() { return requestBodyType; }
    public void setRequestBodyType(String requestBodyType) { this.requestBodyType = requestBodyType; }

    public List<ParameterInfo> getParameters() { return parameters; }
    public void setParameters(List<ParameterInfo> parameters) { this.parameters = parameters; }
}
