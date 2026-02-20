package dev.gan.cvpilot.dto.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AnalysisResponse(
        @JsonProperty("match_score") int matchScore,
        @JsonProperty("match_level") String matchLevel,
        String summary,
        List<StrengthItem> strengths,
        @JsonProperty("partial_matches") List<PartialMatchItem> partialMatches,
        List<GapItem> gaps,
        Recommendations recommendations,
        @JsonProperty("risk_notes") List<String> riskNotes
) {

    public record StrengthItem(String area, String description) {
    }

    public record PartialMatchItem(String requirement, String comment) {
    }

    public record GapItem(
            @JsonProperty("missing_requirement") String missingRequirement,
            String impact
    ) {
    }

    public record Recommendations(
            @JsonProperty("resume_improvements") List<String> resumeImprovements,
            @JsonProperty("skills_to_highlight") List<String> skillsToHighlight,
            @JsonProperty("skills_to_acquire") List<String> skillsToAcquire
    ) {
    }
}
