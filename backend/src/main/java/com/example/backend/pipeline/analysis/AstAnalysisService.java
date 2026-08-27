package com.example.backend.pipeline.analysis;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.RepositoryAnalysis;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.RepositoryAnalysisRepository;
import com.example.backend.common.enums.AnalysisStatus;
import com.example.backend.pipeline.analysis.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AstAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AstAnalysisService.class);

    private final RepositoryAnalysisRepository repositoryAnalysisRepository;
    private final AssessmentRepository assessmentRepository;
    private final ObjectMapper objectMapper;

    public AstAnalysisService(RepositoryAnalysisRepository repositoryAnalysisRepository,
                              AssessmentRepository assessmentRepository) {
        this.repositoryAnalysisRepository = repositoryAnalysisRepository;
        this.assessmentRepository = assessmentRepository;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Executes Phase 3: Uses AST and code analysis to extract complete codebase metadata,
     * routes, entities, fields, repositories, services, and persists into REPOSITORY_ANALYSIS.
     */
    @Transactional
    public AstAnalysisResult analyzeSourceCode(UUID assessmentId, Path repoRootPath, String backendRootDirectory) {
        Path targetBackendDir = repoRootPath;
        if (backendRootDirectory != null && !backendRootDirectory.trim().isEmpty()) {
            Path candidate = repoRootPath.resolve(backendRootDirectory.trim());
            if (Files.exists(candidate)) {
                targetBackendDir = candidate;
            }
        }
        if (!Files.exists(targetBackendDir.resolve("pom.xml")) && Files.exists(repoRootPath.resolve("pom.xml"))) {
            targetBackendDir = repoRootPath;
        }

        log.info("Phase 3: Starting AST Codebase Analysis for Assessment {} in {}", assessmentId, targetBackendDir.toAbsolutePath());

        try {
            // 1. Parse Project Structure & Dependencies (pom.xml)
            ProjectStructureDto projectStructure = parsePomXml(targetBackendDir);

            // 2. Discover all Java source files under src/main/java
            Path srcMainJava = targetBackendDir.resolve("src").resolve("main").resolve("java");
            List<Path> javaFiles = new ArrayList<>();
            Set<String> packageSet = new HashSet<>();

            if (Files.exists(srcMainJava)) {
                try (Stream<Path> stream = Files.walk(srcMainJava)) {
                    javaFiles = stream.filter(p -> p.toString().endsWith(".java") && Files.isRegularFile(p))
                            .collect(Collectors.toList());
                }
            }

            projectStructure.setTotalJavaFiles(javaFiles.size());

            // 3. Analyze each Java file using SpringBootSourceAnalyzer
            SpringBootSourceAnalyzer analyzer = new SpringBootSourceAnalyzer();

            for (Path javaFile : javaFiles) {
                try {
                    String content = Files.readString(javaFile);
                    Matcher pkgMatcher = Pattern.compile("package\\s+([a-zA-Z0-9_.]+);").matcher(content);
                    if (pkgMatcher.find()) {
                        packageSet.add(pkgMatcher.group(1).trim());
                    }
                    analyzer.analyzeJavaSource(content);
                } catch (Exception ex) {
                    log.warn("Failed to parse Java file {}: {}", javaFile.getFileName(), ex.getMessage());
                }
            }

            projectStructure.setPackages(new ArrayList<>(packageSet));

            // Top-level files in backend root
            try (Stream<Path> stream = Files.list(targetBackendDir)) {
                List<String> topLevel = stream.map(p -> p.getFileName().toString()).collect(Collectors.toList());
                projectStructure.setTopLevelFiles(topLevel);
            } catch (Exception ignored) {
            }

            SourceCodeStructureDto sourceCodeStructure = analyzer.getSourceCodeStructure();

            // 4. Build Aggregated Content Details
            ContentDetailsDto contentDetails = buildContentDetails(sourceCodeStructure, projectStructure);

            // 5. Serialize to JSON
            String projectStructureJson = objectMapper.writeValueAsString(projectStructure);
            String sourceCodeStructureJson = objectMapper.writeValueAsString(sourceCodeStructure);
            String contentDetailsJson = objectMapper.writeValueAsString(contentDetails);

            // 6. Persist to PostgreSQL database (if repositories provided)
            persistRepositoryAnalysis(assessmentId, projectStructureJson, sourceCodeStructureJson, contentDetailsJson);

            log.info("Phase 3: AST Codebase Analysis COMPLETED for Assessment {} (Controllers: {}, Entities: {}, Repos: {}, Services: {})",
                    assessmentId,
                    sourceCodeStructure.getControllers().size(),
                    sourceCodeStructure.getEntities().size(),
                    sourceCodeStructure.getRepositories().size(),
                    sourceCodeStructure.getServices().size());

            return AstAnalysisResult.ok(
                    projectStructure,
                    sourceCodeStructure,
                    contentDetails,
                    projectStructureJson,
                    sourceCodeStructureJson,
                    contentDetailsJson
            );

        } catch (Exception ex) {
            log.error("Phase 3: AST Analysis failed for assessment {}: {}", assessmentId, ex.getMessage(), ex);
            return AstAnalysisResult.fail("AST analysis failed: " + ex.getMessage());
        }
    }

    private void persistRepositoryAnalysis(UUID assessmentId, String projJson, String srcJson, String contentJson) {
        if (repositoryAnalysisRepository == null || assessmentId == null) {
            return;
        }

        try {
            RepositoryAnalysis analysis = repositoryAnalysisRepository.findById(assessmentId).orElse(null);
            if (analysis == null) {
                if (assessmentRepository != null) {
                    Assessment assessment = assessmentRepository.findById(assessmentId).orElse(null);
                    if (assessment != null) {
                        analysis = new RepositoryAnalysis(assessment, AnalysisStatus.RUNNING);
                    }
                }
            }

            if (analysis != null) {
                analysis.setProjectStructure(projJson);
                analysis.setSourceCodeStructure(srcJson);
                analysis.setContentDetails(contentJson);
                analysis.setAnalysisStatus(AnalysisStatus.COMPLETED);
                analysis.setCompletedAt(Instant.now());
                repositoryAnalysisRepository.save(analysis);
                log.info("Phase 3: Saved REPOSITORY_ANALYSIS record for assessment {}", assessmentId);
            }
        } catch (Exception ex) {
            log.warn("Could not persist REPOSITORY_ANALYSIS to DB: {}", ex.getMessage());
        }
    }

    private ProjectStructureDto parsePomXml(Path targetBackendDir) {
        ProjectStructureDto dto = new ProjectStructureDto();
        Path pomPath = targetBackendDir.resolve("pom.xml");

        if (!Files.exists(pomPath)) {
            return dto;
        }

        try {
            String content = Files.readString(pomPath);

            // Extract Spring Boot parent version
            Pattern parentVersionPattern = Pattern.compile("<parent>[\\s\\S]*?<artifactId>spring-boot-starter-parent</artifactId>[\\s\\S]*?<version>(.*?)</version>", Pattern.DOTALL);
            Matcher parentMatcher = parentVersionPattern.matcher(content);
            if (parentMatcher.find()) {
                dto.setSpringBootVersion(parentMatcher.group(1).trim());
            }

            // Extract Java version
            Pattern javaVersionPattern = Pattern.compile("<java\\.version>(.*?)</java\\.version>");
            Matcher javaMatcher = javaVersionPattern.matcher(content);
            if (javaMatcher.find()) {
                dto.setJavaVersion(javaMatcher.group(1).trim());
            }

            // Extract dependencies
            Pattern depPattern = Pattern.compile("<dependency>[\\s\\S]*?<groupId>(.*?)</groupId>[\\s\\S]*?<artifactId>(.*?)</artifactId>[\\s\\S]*?</dependency>", Pattern.DOTALL);
            Matcher depMatcher = depPattern.matcher(content);
            List<String> deps = new ArrayList<>();
            while (depMatcher.find()) {
                String groupId = depMatcher.group(1).trim();
                String artifactId = depMatcher.group(2).trim();
                deps.add(groupId + ":" + artifactId);
            }
            dto.setDependencies(deps);

        } catch (IOException ex) {
            log.warn("Could not read pom.xml: {}", ex.getMessage());
        }

        return dto;
    }

    private ContentDetailsDto buildContentDetails(SourceCodeStructureDto sc, ProjectStructureDto ps) {
        ContentDetailsDto cd = new ContentDetailsDto();
        cd.setTotalControllers(sc.getControllers().size());
        cd.setTotalEntities(sc.getEntities().size());
        cd.setTotalRepositories(sc.getRepositories().size());
        cd.setTotalServices(sc.getServices().size());

        int endpointCount = 0;
        List<String> routes = new ArrayList<>();
        for (ControllerInfo ctrl : sc.getControllers()) {
            for (EndpointInfo ep : ctrl.getEndpoints()) {
                endpointCount++;
                routes.add("[" + ep.getHttpMethod() + "] " + ep.getFullPath() + " (" + ctrl.getClassName() + "#" + ep.getHandlerMethod() + ")");
            }
        }
        cd.setTotalEndpoints(endpointCount);
        cd.setExposedRoutes(routes);

        List<String> tables = new ArrayList<>();
        for (EntityInfo entity : sc.getEntities()) {
            tables.add(entity.getTableName() + " (" + entity.getClassName() + ")");
        }
        cd.setDatabaseTables(tables);

        Map<String, Object> summary = new HashMap<>();
        summary.put("language", ps.getLanguage());
        summary.put("javaVersion", ps.getJavaVersion());
        summary.put("springBootVersion", ps.getSpringBootVersion());
        summary.put("totalJavaFiles", ps.getTotalJavaFiles());
        summary.put("totalControllers", sc.getControllers().size());
        summary.put("totalEndpoints", endpointCount);
        summary.put("totalEntities", sc.getEntities().size());
        summary.put("totalRepositories", sc.getRepositories().size());
        summary.put("totalServices", sc.getServices().size());
        cd.setSummaryMetrics(summary);

        return cd;
    }
}
