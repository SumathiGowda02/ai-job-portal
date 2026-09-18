package com.jobportal.service;

import com.jobportal.dto.JobRequest;
import com.jobportal.dto.JobResponse;
import com.jobportal.dto.PagedResult;
import com.jobportal.exception.ForbiddenException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    @CacheEvict(value = "jobSearch", allEntries = true)
    public JobResponse createJob(JobRequest request, User recruiter) {
        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .jobType(request.getJobType())
                .requiredSkills(request.getRequiredSkills())
                .minSalary(request.getMinSalary())
                .maxSalary(request.getMaxSalary())
                .recruiter(recruiter)
                .build();
        return JobResponse.from(jobRepository.save(job));
    }

    @CacheEvict(value = "jobSearch", allEntries = true)
    public JobResponse updateJob(Long jobId, JobRequest request, User recruiter) {
        Job job = getJobOrThrow(jobId);
        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new ForbiddenException("You can only edit your own job postings");
        }
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setJobType(request.getJobType());
        job.setRequiredSkills(request.getRequiredSkills());
        job.setMinSalary(request.getMinSalary());
        job.setMaxSalary(request.getMaxSalary());
        return JobResponse.from(jobRepository.save(job));
    }

    @CacheEvict(value = "jobSearch", allEntries = true)
    public void deleteJob(Long jobId, User recruiter) {
        Job job = getJobOrThrow(jobId);
        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new ForbiddenException("You can only delete your own job postings");
        }
        jobRepository.delete(job);
    }

    public JobResponse getJob(Long jobId) {
        return JobResponse.from(getJobOrThrow(jobId));
    }

    public Job getJobOrThrow(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
    }

    public Page<JobResponse> myJobs(User recruiter, Pageable pageable) {
        return jobRepository.findByRecruiter(recruiter, pageable).map(JobResponse::from);
    }

     @Cacheable(value = "jobSearch", key = "#keyword + '-' + #location + '-' + #jobType + '-' + #pageable.pageNumber")
        public PagedResult<JobResponse> search(String keyword, String location, Job.JobType jobType, Pageable pageable) {
        return PagedResult.from(jobRepository.search(keyword, location, jobType, pageable).map(JobResponse::from));
  }
}
