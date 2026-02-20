package dev.gan.cvpilot.dto.analysis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnalysisRequest(

        @NotBlank(message = "Vacancy text is required")
        @Size(min = 50, max = 15000, message = "Vacancy text must be between 50 and 15000 characters")
        String vacancyText,

        @NotBlank(message = "Resume text is required")
        @Size(min = 50, max = 20000, message = "Resume text must be between 50 and 20000 characters")
        String resumeText,

        @Size(max = 300, message = "Vacancy title must not exceed 300 characters")
        String vacancyTitle,

        @Size(max = 300, message = "Company name must not exceed 300 characters")
        String companyName
) {
}
