package dev.gan.cvpilot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gan.cvpilot.dto.analysis.*;
import dev.gan.cvpilot.entity.User;
import dev.gan.cvpilot.exception.LimitExceededException;
import dev.gan.cvpilot.exception.LlmException;
import dev.gan.cvpilot.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
public class AnalysisService {

    private static final int FREE_DAILY_LIMIT = 3;

    private static final String SYSTEM_PROMPT = """
            You are an AI assistant at the level of a Senior HR Partner and Technical Recruiter \
            with experience recruiting specialists in IT, Digital, Product, Analytics, and Management.

            You analyze how well a resume matches a specific job posting.

            Your task:
            - objectively assess how the resume matches the job requirements
            - identify strengths and gaps
            - provide practical recommendations for improving the resume for this specific job

            IMPORTANT:
            - Do not fabricate experience, skills, or facts that are not in the resume
            - Analyze only the provided texts
            - Be neutral, professional, and specific
            - Always respond in Russian, regardless of the language of the vacancy or resume
            - Each description should be 1-2 sentences; recommendations must be specific and actionable
            - Always return the response strictly in JSON format, without markdown wrapping or comments""";

    private static final String USER_PROMPT_TEMPLATE = """
            Analyze the resume-to-vacancy match.

            <vacancy>
            {vacancy_text}
            </vacancy>

            <resume>
            {resume_text}
            </resume>

            ### ANALYSIS REQUIREMENTS

            1. Rate the resume-to-vacancy match on a 100-point scale
            2. Analyze:
               - professional skills
               - work experience
               - technologies / tools
               - level of responsibility
               - domain relevance (if applicable)
            3. Identify:
               - fully matching requirements
               - partially matching requirements
               - missing or weak areas
            4. Provide recommendations:
               - what to improve or add to the resume
               - which phrasings to strengthen
               - which skills to emphasize

            ### match_level SCORING RULES

            Determine match_level strictly by these boundaries:
            - 0-30   -> "низкое"
            - 31-60  -> "среднее"
            - 61-80  -> "хорошее"
            - 81-100 -> "отличное"

            ### RESPONSE FORMAT (STRICT JSON)

            {
              "match_score": number,
              "match_level": string,
              "summary": string,
              "strengths": [
                { "area": string, "description": string }
              ],
              "partial_matches": [
                { "requirement": string, "comment": string }
              ],
              "gaps": [
                { "missing_requirement": string, "impact": string }
              ],
              "recommendations": {
                "resume_improvements": [string],
                "skills_to_highlight": [string],
                "skills_to_acquire": [string]
              },
              "risk_notes": [string]
            }

            Do not add any fields beyond those specified.
            If information is insufficient - explicitly indicate this in the relevant fields.
            {plan_instruction}""";

    private static final String FREE_PLAN_INSTRUCTION =
            "\nAdditional constraint: keep descriptions brief (1 sentence each), maximum 3 items per category.";

    private static final String PRO_PLAN_INSTRUCTION = "";

    private final ChatClient chatClient;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.anthropic.chat.options.model:claude-sonnet-4-20250514}")
    private String modelName;

    public AnalysisService(ChatClient.Builder chatClientBuilder, UserService userService, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    public FullAnalysisResponse analyzeMatch(Long userId, AnalysisRequest request) {
        long startTime = System.currentTimeMillis();

        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Lazy reset: if analysesDate != today, reset counter
        LocalDate today = LocalDate.now();
        if (!today.equals(user.getAnalysesDate())) {
            user.setAnalysesToday(0);
            user.setAnalysesDate(today);
        }

        // Check daily limit for FREE plan
        if ("FREE".equals(user.getPlan()) && user.getAnalysesToday() >= FREE_DAILY_LIMIT) {
            throw new LimitExceededException(user.getPlan());
        }

        String planInstruction = "FREE".equals(user.getPlan()) ? FREE_PLAN_INSTRUCTION : PRO_PLAN_INSTRUCTION;

        String userPrompt = USER_PROMPT_TEMPLATE
                .replace("{vacancy_text}", escapeXml(request.vacancyText()))
                .replace("{resume_text}", escapeXml(request.resumeText()))
                .replace("{plan_instruction}", planInstruction);

        // Call Claude via Spring AI
        AnalysisResponse analysisResponse = callLlm(userPrompt, true);

        // Increment counter and save
        user.setAnalysesToday(user.getAnalysesToday() + 1);
        user.setAnalysesDate(today);
        userService.save(user);

        long processingTime = System.currentTimeMillis() - startTime;
        int remaining = "FREE".equals(user.getPlan())
                ? FREE_DAILY_LIMIT - user.getAnalysesToday()
                : -1; // -1 = unlimited

        AnalysisMetadata metadata = new AnalysisMetadata(
                modelName,
                processingTime,
                "v1",
                user.getPlan(),
                remaining
        );

        return new FullAnalysisResponse(analysisResponse, metadata);
    }

    private AnalysisResponse callLlm(String userPrompt, boolean allowRetry) {
        String content;
        try {
            content = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("LLM call failed", e);
            throw new LlmException("Failed to get response from AI model: " + e.getMessage());
        }

        log.debug("LLM raw response: {}", content);

        // Strip markdown code fences if present
        String json = stripMarkdownFences(content);

        try {
            return objectMapper.readValue(json, AnalysisResponse.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse LLM response as JSON: {}", e.getMessage());
            if (allowRetry) {
                log.info("Retrying LLM call after JSON parse failure");
                return callLlm(userPrompt, false);
            }
            throw new LlmException("AI model returned invalid response format");
        }
    }

    private static String stripMarkdownFences(String content) {
        if (content == null) return "";
        String trimmed = content.strip();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.strip();
    }

    private static String escapeXml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
