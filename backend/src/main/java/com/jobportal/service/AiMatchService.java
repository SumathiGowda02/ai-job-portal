package com.jobportal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiMatchService {

    @Value("${ai.provider-url}")
    private String providerUrl;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public record MatchResult(
            int score,
            String summary,
            List<String> matchedSkills,
            List<String> missingSkills
    ) {}

    public MatchResult match(
            String resumeText,
            String jobDescription,
            List<String> requiredSkills
    ) {

        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return new MatchResult(
                    50,
                    "No required skills listed to compare against.",
                    List.of(),
                    List.of()
            );
        }

        /*
         * If an external AI provider is configured without an API key,
         * use the deterministic keyword fallback.
         */
        if (!providerUrl.contains("localhost:11434")
                && (apiKey == null || apiKey.isBlank())) {

            log.warn(
                    "AI_API_KEY not configured — falling back to keyword-overlap scoring"
            );

            return fallbackKeywordMatch(
                    resumeText,
                    requiredSkills
            );
        }

        try {

            log.info(
                    "AI matching started. Provider: {}, Model: {}",
                    providerUrl,
                    model
            );

            String prompt = buildPrompt(
                    resumeText,
                    jobDescription,
                    requiredSkills
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            /*
             * These headers are only required for external
             * Anthropic-compatible providers.
             *
             * Ollama does not need them.
             */
            if (!providerUrl.contains("localhost:11434")) {
                headers.set("x-api-key", apiKey);
                headers.set("anthropic-version", "2023-06-01");
            }

            Map<String, Object> body = Map.of(
                    "model", model,
                    "stream", false,
                    "messages", List.of(
                            Map.of(
                                    "role",
                                    "user",
                                    "content",
                                    prompt
                            )
                    )
            );

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            JsonNode response =
                    restTemplate.postForObject(
                            providerUrl,
                            entity,
                            JsonNode.class
                    );

            String text = response
                    .path("message")
                    .path("content")
                    .asText();

            log.info("AI RAW RESPONSE: {}", text);

            String jsonPart = extractJson(text);

            JsonNode parsed = mapper.readTree(jsonPart);

            /*
             * Get the skills identified by the AI.
             */
            List<String> aiMatchedSkills =
                    toList(parsed.path("matchedSkills"));

            List<String> aiMissingSkills =
                    toList(parsed.path("missingSkills"));

            /*
             * Keep ONLY skills that belong to the job's
             * required-skills list.
             */
            List<String> matchedSkills =
                    filterRequiredSkills(
                            aiMatchedSkills,
                            requiredSkills
                    );

            List<String> missingSkills =
                    filterRequiredSkills(
                            aiMissingSkills,
                            requiredSkills
                    );

            /*
             * Make the backend score deterministic.
             *
             * Score = matched required skills / total required skills * 100
             *
             * Example:
             * 4 matched out of 6 = 67%
             * 5 matched out of 6 = 83%
             * 6 matched out of 6 = 100%
             */
            int score = calculateSkillScore(
                    matchedSkills,
                    requiredSkills
            );

            String summary =
                    parsed.path("summary").asText("");

            if (summary.isBlank()) {
                summary =
                        "The candidate matches "
                                + matchedSkills.size()
                                + " out of "
                                + requiredSkills.size()
                                + " required skills.";
            }

            log.info(
                    "AI matching completed. Score: {}%, Matched: {}, Missing: {}",
                    score,
                    matchedSkills,
                    missingSkills
            );

            return new MatchResult(
                    score,
                    summary,
                    matchedSkills,
                    missingSkills
            );

        } catch (Exception e) {

            log.error(
                    "AI match call failed, falling back to keyword match: {}",
                    e.getMessage()
            );

            return fallbackKeywordMatch(
                    resumeText,
                    requiredSkills
            );
        }
    }

    private String buildPrompt(
            String resumeText,
            String jobDescription,
            List<String> requiredSkills
    ) {

        String skills =
                String.join(
                        ", ",
                        requiredSkills
                );

        return """
                You are an ATS resume screening system.

                Compare the RESUME against the JOB DESCRIPTION
                and REQUIRED SKILLS.

                IMPORTANT RULES:

                1. matchedSkills MUST contain ONLY skills from
                   REQUIRED_SKILLS that are clearly present in
                   the resume.

                2. missingSkills MUST contain ONLY skills from
                   REQUIRED_SKILLS that are not clearly present
                   in the resume.

                3. Do NOT add unrelated skills from the resume.

                4. Do NOT invent skills.

                5. Consider obvious skill variations.
                   Examples:
                   - "Java programming" can match "Java"
                   - "RESTful APIs" can match "REST API"
                   - "Spring Boot framework" can match "Spring Boot"

                6. The backend will calculate the final score
                   from the required-skill match.

                7. Therefore, do NOT try to artificially increase
                   the score.

                8. Return ONLY valid JSON.
                   Do not use markdown.
                   Do not include text outside the JSON.

                Required JSON format:

                {
                  "score": 0,
                  "summary": "2-3 sentence explanation",
                  "matchedSkills": ["skill1", "skill2"],
                  "missingSkills": ["skill3"]
                }

                REQUIRED_SKILLS:
                %s

                JOB_DESCRIPTION:
                %s

                RESUME:
                %s
                """.formatted(
                skills,
                jobDescription,
                resumeText
        );
    }

    /*
     * Keeps only AI-returned skills that actually exist
     * in the required-skills list.
     *
     * Matching is case-insensitive.
     */
    private List<String> filterRequiredSkills(
            List<String> aiSkills,
            List<String> requiredSkills
    ) {

        if (aiSkills == null || aiSkills.isEmpty()) {
            return List.of();
        }

        return requiredSkills.stream()
                .filter(requiredSkill ->
                        aiSkills.stream()
                                .anyMatch(aiSkill ->
                                        aiSkill != null
                                                && aiSkill.equalsIgnoreCase(
                                                requiredSkill
                                        )
                                )
                )
                .toList();
    }

    /*
     * Calculates a predictable ATS-style skill score.
     */
    private int calculateSkillScore(
            List<String> matchedSkills,
            List<String> requiredSkills
    ) {

        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return 50;
        }

        return (int) Math.round(
                matchedSkills.size() * 100.0
                        / requiredSkills.size()
        );
    }

    private String extractJson(String text) {

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start >= 0 && end > start) {
            return text.substring(
                    start,
                    end + 1
            );
        }

        return "{}";
    }

    private List<String> toList(JsonNode node) {

        if (node != null && node.isArray()) {

            return mapper.convertValue(
                    node,
                    List.class
            );
        }

        return List.of();
    }

    /*
     * Deterministic fallback when the AI provider is unavailable.
     */
    private MatchResult fallbackKeywordMatch(
            String resumeText,
            List<String> requiredSkills
    ) {

        if (requiredSkills == null || requiredSkills.isEmpty()) {

            return new MatchResult(
                    50,
                    "No required skills listed to compare against.",
                    List.of(),
                    List.of()
            );
        }

        String lowerResume =
                resumeText == null
                        ? ""
                        : resumeText.toLowerCase();

        List<String> matched =
                requiredSkills.stream()
                        .filter(skill ->
                                lowerResume.contains(
                                        skill.toLowerCase()
                                )
                        )
                        .toList();

        List<String> missing =
                requiredSkills.stream()
                        .filter(skill ->
                                !matched.contains(skill)
                        )
                        .toList();

        int score =
                (int) Math.round(
                        matched.size() * 100.0
                                / requiredSkills.size()
                );

        String summary =
                "Keyword-based match (AI provider not configured): "
                        + matched.size()
                        + "/"
                        + requiredSkills.size()
                        + " required skills found.";

        return new MatchResult(
                score,
                summary,
                matched,
                missing
        );
    }
}