package com.example.backend.pipeline.analysis;

import com.example.backend.pipeline.analysis.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpringBootSourceAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(SpringBootSourceAnalyzer.class);

    private final SourceCodeStructureDto sourceCodeStructure = new SourceCodeStructureDto();

    public void analyzeJavaSource(String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        String packageName = extractPackage(content);

        if (isRestController(content)) {
            processController(content, packageName);
        } else if (isEntity(content)) {
            processEntity(content, packageName);
        } else if (isRepository(content)) {
            processRepository(content, packageName);
        } else if (isService(content)) {
            processService(content, packageName);
        }
    }

    public SourceCodeStructureDto getSourceCodeStructure() {
        return sourceCodeStructure;
    }

    private String extractPackage(String content) {
        Matcher m = Pattern.compile("package\\s+([a-zA-Z0-9_.]+);").matcher(content);
        return m.find() ? m.group(1).trim() : "";
    }

    private boolean isRestController(String content) {
        return content.contains("@RestController") || content.contains("@Controller");
    }

    private boolean isEntity(String content) {
        return content.contains("@Entity") || (content.contains("@Table") && content.contains("@Id"));
    }

    private boolean isRepository(String content) {
        return content.contains("extends JpaRepository") || content.contains("extends CrudRepository")
                || content.contains("extends PagingAndSortingRepository") || content.contains("@Repository");
    }

    private boolean isService(String content) {
        return content.contains("@Service");
    }

    private void processController(String content, String packageName) {
        String className = extractClassName(content);
        String basePath = "";

        Matcher rmMatcher = Pattern.compile("@RequestMapping\\(\\s*(?:(?:value|path)\\s*=\\s*)?\"([^\"]*)\"\\s*\\)").matcher(content);
        if (rmMatcher.find()) {
            basePath = rmMatcher.group(1).trim();
        }

        ControllerInfo controllerInfo = new ControllerInfo(className, packageName, basePath);

        // Extract endpoints
        Pattern methodPattern = Pattern.compile(
                "@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\\s*(?:\\(\\s*(?:(?:value|path)\\s*=\\s*)?\"([^\"]*)\"\\s*\\))?[\\s\\S]*?public\\s+([\\w<>,\\s\\[\\]]+)\\s+([a-zA-Z0-9_]+)\\s*\\(([^)]*)\\)",
                Pattern.MULTILINE
        );

        Matcher mMatcher = methodPattern.matcher(content);
        while (mMatcher.find()) {
            String annotationType = mMatcher.group(1);
            String subPath = mMatcher.group(2) != null ? mMatcher.group(2).trim() : "";
            String returnType = mMatcher.group(3).trim();
            String methodName = mMatcher.group(4).trim();
            String rawParams = mMatcher.group(5).trim();

            String httpMethod = switch (annotationType) {
                case "GetMapping" -> "GET";
                case "PostMapping" -> "POST";
                case "PutMapping" -> "PUT";
                case "DeleteMapping" -> "DELETE";
                case "PatchMapping" -> "PATCH";
                default -> "REQUEST";
            };

            String fullPath = combinePaths(basePath, subPath);
            EndpointInfo ep = new EndpointInfo(httpMethod, subPath, fullPath, methodName, returnType, null);

            // Parse Parameters
            if (!rawParams.isEmpty()) {
                String[] params = rawParams.split(",");
                for (String param : params) {
                    param = param.trim();
                    if (param.isEmpty()) continue;

                    String paramAnn = null;
                    if (param.contains("@PathVariable")) paramAnn = "PathVariable";
                    else if (param.contains("@RequestBody")) {
                        paramAnn = "RequestBody";
                    } else if (param.contains("@RequestParam")) paramAnn = "RequestParam";
                    else if (param.contains("@RequestHeader")) paramAnn = "RequestHeader";

                    // Clean annotations from param string
                    String cleanParam = param.replaceAll("@[a-zA-Z0-9_]+(?:\\([^)]*\\))?", "").trim();
                    String[] tokens = cleanParam.split("\\s+");
                    if (tokens.length >= 2) {
                        String pType = tokens[0].trim();
                        String pName = tokens[tokens.length - 1].trim();
                        if ("RequestBody".equals(paramAnn)) {
                            ep.setRequestBodyType(pType);
                        }
                        ep.getParameters().add(new ParameterInfo(pName, pType, paramAnn, true));
                    }
                }
            }

            controllerInfo.getEndpoints().add(ep);
        }

        sourceCodeStructure.getControllers().add(controllerInfo);
    }

    private void processEntity(String content, String packageName) {
        String className = extractClassName(content);
        String tableName = className.toLowerCase();

        Matcher tableMatcher = Pattern.compile("@Table\\s*\\(\\s*name\\s*=\\s*\"([^\"]+)\"\\s*\\)").matcher(content);
        if (tableMatcher.find()) {
            tableName = tableMatcher.group(1).trim();
        }

        EntityInfo entityInfo = new EntityInfo(className, tableName, packageName);

        Pattern fieldPattern = Pattern.compile(
                "((?:@[a-zA-Z0-9_]+(?:\\([^)]*\\))?\\s*)*)private\\s+([\\w<>,\\s\\[\\]]+)\\s+([a-zA-Z0-9_]+)\\s*;",
                Pattern.MULTILINE
        );

        Matcher fMatcher = fieldPattern.matcher(content);
        while (fMatcher.find()) {
            String annotations = fMatcher.group(1) != null ? fMatcher.group(1).trim() : "";
            String fieldType = fMatcher.group(2).trim();
            String fieldName = fMatcher.group(3).trim();

            boolean isId = annotations.contains("@Id");
            FieldInfo fieldInfo = new FieldInfo(fieldName, fieldType, isId);

            if (annotations.contains("@OneToMany")) {
                fieldInfo.setRelation("OneToMany");
                fieldInfo.setTargetEntity(fieldType);
                entityInfo.getRelations().add("OneToMany -> " + fieldType);
            } else if (annotations.contains("@ManyToOne")) {
                fieldInfo.setRelation("ManyToOne");
                fieldInfo.setTargetEntity(fieldType);
                entityInfo.getRelations().add("ManyToOne -> " + fieldType);
            } else if (annotations.contains("@ManyToMany")) {
                fieldInfo.setRelation("ManyToMany");
                fieldInfo.setTargetEntity(fieldType);
                entityInfo.getRelations().add("ManyToMany -> " + fieldType);
            } else if (annotations.contains("@OneToOne")) {
                fieldInfo.setRelation("OneToOne");
                fieldInfo.setTargetEntity(fieldType);
                entityInfo.getRelations().add("OneToOne -> " + fieldType);
            }

            entityInfo.getFields().add(fieldInfo);
        }

        sourceCodeStructure.getEntities().add(entityInfo);
    }

    private void processRepository(String content, String packageName) {
        String interfaceName = extractInterfaceName(content);
        String domainEntity = "Unknown";
        String idType = "Object";

        Matcher repoMatcher = Pattern.compile("extends\\s+(?:JpaRepository|CrudRepository|PagingAndSortingRepository)\\s*<\\s*([a-zA-Z0-9_]+)\\s*,\\s*([a-zA-Z0-9_]+)\\s*>").matcher(content);
        if (repoMatcher.find()) {
            domainEntity = repoMatcher.group(1).trim();
            idType = repoMatcher.group(2).trim();
        }

        RepositoryInfo repoInfo = new RepositoryInfo(interfaceName, packageName, domainEntity, idType);

        // Find custom method declarations
        Pattern methodPattern = Pattern.compile("([\\w<>,\\s\\[\\]]+)\\s+([a-zA-Z0-9_]+)\\s*\\(([^)]*)\\)\\s*;");
        Matcher mMatcher = methodPattern.matcher(content);
        while (mMatcher.find()) {
            repoInfo.getMethods().add(mMatcher.group(1) + " " + mMatcher.group(2) + "(" + mMatcher.group(3) + ")");
        }

        sourceCodeStructure.getRepositories().add(repoInfo);
    }

    private void processService(String content, String packageName) {
        String className = extractClassName(content);
        ServiceInfo serviceInfo = new ServiceInfo(className, packageName);

        Pattern methodPattern = Pattern.compile("public\\s+([\\w<>,\\s\\[\\]]+)\\s+([a-zA-Z0-9_]+)\\s*\\(([^)]*)\\)\\s*\\{");
        Matcher mMatcher = methodPattern.matcher(content);
        while (mMatcher.find()) {
            serviceInfo.getMethods().add(mMatcher.group(1) + " " + mMatcher.group(2) + "(" + mMatcher.group(3) + ")");
        }

        sourceCodeStructure.getServices().add(serviceInfo);
    }

    private String extractClassName(String content) {
        Matcher m = Pattern.compile("public\\s+class\\s+([a-zA-Z0-9_]+)").matcher(content);
        return m.find() ? m.group(1).trim() : "UnknownClass";
    }

    private String extractInterfaceName(String content) {
        Matcher m = Pattern.compile("public\\s+interface\\s+([a-zA-Z0-9_]+)").matcher(content);
        return m.find() ? m.group(1).trim() : "UnknownInterface";
    }

    private String combinePaths(String basePath, String subPath) {
        if (basePath == null) basePath = "";
        if (subPath == null) subPath = "";

        if (!basePath.startsWith("/") && !basePath.isEmpty()) basePath = "/" + basePath;
        if (basePath.endsWith("/")) basePath = basePath.substring(0, basePath.length() - 1);

        if (!subPath.startsWith("/") && !subPath.isEmpty()) subPath = "/" + subPath;

        String combined = basePath + subPath;
        return combined.isEmpty() ? "/" : combined;
    }
}
