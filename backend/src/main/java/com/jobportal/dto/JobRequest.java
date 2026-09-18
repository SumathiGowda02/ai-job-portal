package com.jobportal.dto;

import com.jobportal.model.Job;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class JobRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String location;

    private Job.JobType jobType;
    private List<String> requiredSkills;
    private Double minSalary;
    private Double maxSalary;
}
