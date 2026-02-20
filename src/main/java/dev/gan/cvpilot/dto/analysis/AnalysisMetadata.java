package dev.gan.cvpilot.dto.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnalysisMetadata(
        @JsonProperty("model_used") String modelUsed,
        @JsonProperty("processing_time_ms") long processingTimeMs,
        @JsonProperty("api_version") String apiVersion,
        String plan,
        @JsonProperty("remaining_today") int remainingToday
) {
}
