package dev.gan.cvpilot.dto.analysis;

public record FullAnalysisResponse(
        AnalysisResponse analysis,
        AnalysisMetadata metadata
) {
}
