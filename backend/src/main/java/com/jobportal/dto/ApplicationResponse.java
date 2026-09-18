package com.jobportal.dto;

import com.jobportal.model.Application;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String candidateName;
    private String resumeFileName;
    private String resumeUrl;
    private Integer matchScore;
    private String matchSummary;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String status;
    private LocalDateTime appliedAt;

    public static ApplicationResponse from(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .candidateName(app.getCandidate().getFullName())
                .resumeFileName(app.getResume().getOriginalFileName())
                .resumeUrl(app.getResume().getS3Url())
                .matchScore(app.getMatchScore())
                .matchSummary(app.getMatchSummary())
                .matchedSkills(app.getMatchedSkills())
                .missingSkills(app.getMissingSkills())
                .status(app.getStatus().name())
                .appliedAt(app.getAppliedAt())
                .build();
    }
}
