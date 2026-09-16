package com.example.backend.pipeline.workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.backend.pipeline.git.GitCloningService;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class RepositoryWorkspaceInitializer {

    private static final Logger log = LoggerFactory.getLogger(RepositoryWorkspaceInitializer.class);

    private final GitCloningService gitCloningService;

    private static final Set<String> IGNORED_COPY_NAMES = Set.of(
            ".git", "target", ".idea", "node_modules", ".settings", ".classpath", ".project", ".DS_Store"
    );
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".class", ".war", ".nar", ".zip", ".tar", ".gz"
    );

    public RepositoryWorkspaceInitializer(GitCloningService gitCloningService) {
        this.gitCloningService = gitCloningService;
    }

    public void cloneRepositoryIfNeeded(UUID assessmentId, String repoUrl, String branch, Path originalRepoPath) {
        if (!Files.exists(originalRepoPath) && repoUrl != null && !repoUrl.trim().isEmpty()) {
            log.info("Cloning repository on demand for candidate workspace: {}", repoUrl);
            gitCloningService.cloneRepository(assessmentId, repoUrl, branch);
        }
    }

    public void initializeWorkspace(Path originalRepoPath, Path candidateWorkspacePath) throws IOException {
        if (!Files.exists(candidateWorkspacePath)) {
            Files.createDirectories(candidateWorkspacePath);
            if (Files.exists(originalRepoPath)) {
                copyDirectoryTree(originalRepoPath, candidateWorkspacePath);
                if (!hasAnyPomXml(candidateWorkspacePath)) {
                    createStarterFilesIfMissing(candidateWorkspacePath);
                }
                log.info("Copied original repository from {} to candidate workspace {}", originalRepoPath, candidateWorkspacePath);
            } else {
                createStarterFilesIfMissing(candidateWorkspacePath);
                log.info("Created starter template files in candidate workspace {}", candidateWorkspacePath);
            }
        } else {
            cleanupAccidentalRootStarterFiles(candidateWorkspacePath);
        }
    }

    public void cleanupAccidentalRootStarterFiles(Path candidateWorkspacePath) {
        try {
            boolean hasSubdirectoryPom = false;
            try (Stream<Path> stream = Files.walk(candidateWorkspacePath, 2)) {
                hasSubdirectoryPom = stream.anyMatch(p -> p.getFileName().toString().equals("pom.xml") && !p.getParent().equals(candidateWorkspacePath));
            }

            if (hasSubdirectoryPom) {
                Path rootPom = candidateWorkspacePath.resolve("pom.xml");
                if (Files.exists(rootPom)) {
                    String content = Files.readString(rootPom);
                    if (content.contains("<artifactId>assessment-app</artifactId>")) {
                        Files.deleteIfExists(rootPom);
                        log.info("Cleaned up accidental root starter pom.xml at {}", rootPom);
                    }
                }

                Path rootSrc = candidateWorkspacePath.resolve("src");
                Path dummyPkg = rootSrc.resolve("main").resolve("java").resolve("com").resolve("example");
                if (Files.exists(dummyPkg)) {
                    try (Stream<Path> stream = Files.list(dummyPkg)) {
                        if (stream.findAny().isEmpty()) {
                            deleteRecursively(rootSrc);
                            log.info("Cleaned up accidental empty root src directory at {}", rootSrc);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not clean up root starter files: {}", e.getMessage());
        }
    }

    private boolean hasAnyPomXml(Path dir) {
        try (Stream<Path> stream = Files.walk(dir, 3)) {
            return stream.anyMatch(p -> p.getFileName().toString().equals("pom.xml"));
        } catch (IOException e) {
            return false;
        }
    }

    private void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private boolean shouldIgnoreForCopy(String name) {
        if (IGNORED_COPY_NAMES.contains(name)) return true;
        for (String ext : IGNORED_EXTENSIONS) {
            if (name.endsWith(ext)) return true;
        }
        return false;
    }

    private void copyDirectoryTree(Path source, Path destination) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (shouldIgnoreForCopy(dir.getFileName().toString())) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                Path targetDir = destination.resolve(source.relativize(dir));
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (shouldIgnoreForCopy(file.getFileName().toString())) {
                    return FileVisitResult.CONTINUE;
                }
                Path targetFile = destination.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void createStarterFilesIfMissing(Path candidateWorkspacePath) throws IOException {
        Path srcMainJava = candidateWorkspacePath.resolve("src").resolve("main").resolve("java").resolve("com").resolve("example");
        Files.createDirectories(srcMainJava);

        Path appJava = srcMainJava.resolve("Application.java");
        if (!Files.exists(appJava)) {
            String appContent = """
                    package com.example;

                    import java.io.IOException;
                    import java.io.OutputStream;
                    import java.net.InetSocketAddress;
                    import com.sun.net.httpserver.HttpServer;
                    import com.sun.net.httpserver.HttpHandler;
                    import com.sun.net.httpserver.HttpExchange;

                    public class Application {
                        public static void main(String[] args) throws IOException {
                            int port = 8080;
                            for (String arg : args) {
                                if (arg.startsWith("--server.port=")) {
                                    try {
                                        port = Integer.parseInt(arg.substring("--server.port=".length()));
                                    } catch (NumberFormatException ignored) {}
                                }
                            }
                            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
                            server.createContext("/", new HttpHandler() {
                                @Override
                                public void handle(HttpExchange exchange) throws IOException {
                                    String response = "{\\"status\\":\\"UP\\",\\"message\\":\\"Candidate application is running!\\"}";
                                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                                    exchange.sendResponseHeaders(200, response.getBytes().length);
                                    try (OutputStream os = exchange.getResponseBody()) {
                                        os.write(response.getBytes());
                                    }
                                }
                            });
                            server.setExecutor(null);
                            server.start();
                            System.out.println("Application started on port " + port);
                        }
                    }
                    """;
            Files.writeString(appJava, appContent);
        }

        Path pomXml = candidateWorkspacePath.resolve("pom.xml");
        if (!Files.exists(pomXml)) {
            String defaultPom = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <project xmlns="http://maven.apache.org/POM/4.0.0"
                             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                        <modelVersion>4.0.0</modelVersion>
                        <groupId>com.example</groupId>
                        <artifactId>assessment-app</artifactId>
                        <version>1.0.0</version>
                        <properties>
                            <maven.compiler.source>21</maven.compiler.source>
                            <maven.compiler.target>21</maven.compiler.target>
                            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        </properties>
                        <build>
                            <plugins>
                                <plugin>
                                    <groupId>org.apache.maven.plugins</groupId>
                                    <artifactId>maven-jar-plugin</artifactId>
                                    <version>3.4.2</version>
                                    <configuration>
                                        <archive>
                                            <manifest>
                                                <mainClass>com.example.Application</mainClass>
                                            </manifest>
                                        </archive>
                                    </configuration>
                                </plugin>
                            </plugins>
                        </build>
                    </project>
                    """;
            Files.writeString(pomXml, defaultPom);
        }
    }
}
