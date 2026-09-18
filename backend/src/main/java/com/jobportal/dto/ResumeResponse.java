package com.jobportal.dto;

import com.jobportal.model.Resume;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResumeResponse {
    private Long id;
    private String originalFileName;
    private String s3Url;
    private LocalDateTime uploadedAt;

    public static ResumeResponse from(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .originalFileName(resume.getOriginalFileName())
                .s3Url(resume.getS3Url())
                .uploadedAt(resume.getUploadedAt())
                .build();
    }
}