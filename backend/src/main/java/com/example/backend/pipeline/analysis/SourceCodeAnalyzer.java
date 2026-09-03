package com.example.backend.pipeline.analysis;

import com.example.backend.pipeline.analysis.dto.SourceCodeStructureDto;
import java.nio.file.Path;
import java.util.List;

public interface SourceCodeAnalyzer {
    SourceCodeStructureDto analyzeSourceCode(List<Path> javaFiles);
}
