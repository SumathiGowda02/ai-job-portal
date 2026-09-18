package com.jobportal.dto;

import com.jobportal.model.Job;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private Job.JobType jobType;
    private List<String> requiredSkills;
    private Double minSalary;
    private Double maxSalary;
    private String recruiterName;
    private String companyName;
    private boolean active;
    private LocalDateTime createdAt;

    public static JobResponse from(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .requiredSkills(new ArrayList<>(job.getRequiredSkills()))
                .minSalary(job.getMinSalary())
                .maxSalary(job.getMaxSalary())
                .recruiterName(job.getRecruiter().getFullName())
                .companyName(job.getRecruiter().getCompanyName())
                .active(job.isActive())
                .createdAt(job.getCreatedAt())
                .build();
    }
}