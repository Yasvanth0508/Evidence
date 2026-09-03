package com.example.backend.pipeline.workspace;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;
import java.io.File;

@Service
public class FileService {

    public String readFile(Path targetFile) throws IOException {
        return Files.readString(targetFile);
    }

    public long getFileSize(Path targetFile) throws IOException {
        return Files.size(targetFile);
    }

    public void writeFile(Path targetFile, String content) throws IOException {
        if (targetFile.getParent() != null && !Files.exists(targetFile.getParent())) {
            Files.createDirectories(targetFile.getParent());
        }
        Files.writeString(targetFile, content != null ? content : "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    public void createDirectory(Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
    }

    public void deleteRecursively(Path root) throws IOException {
        if (Files.exists(root)) {
            if (Files.isDirectory(root)) {
                try (Stream<Path> walk = Files.walk(root)) {
                    walk.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            } else {
                Files.deleteIfExists(root);
            }
        }
    }

    public void moveFile(Path oldPath, Path newPath) throws IOException {
        if (newPath.getParent() != null && !Files.exists(newPath.getParent())) {
            Files.createDirectories(newPath.getParent());
        }
        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
    }
}
