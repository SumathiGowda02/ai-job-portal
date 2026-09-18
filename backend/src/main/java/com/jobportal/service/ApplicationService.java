package com.jobportal.service;

import com.jobportal.dto.ApplicationResponse;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ForbiddenException;
import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.model.Resume;
import com.jobportal.model.User;
import com.jobportal.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobService jobService;
    private final ResumeService resumeService;
    private final AiMatchService aiMatchService;

    public ApplicationResponse apply(Long jobId, Long resumeId, User candidate) {
        Job job = jobService.getJobOrThrow(jobId);
        Resume resume = resumeService.getResumeOrThrow(resumeId);

        if (!resume.getCandidate().getId().equals(candidate.getId())) {
            throw new ForbiddenException("You can only apply with your own resume");
        }
        if (applicationRepository.findByJobAndCandidate(job, candidate).isPresent()) {
            throw new BadRequestException("You have already applied to this job");
        }

        AiMatchService.MatchResult match = aiMatchService.match(
                resume.getExtractedText(), job.getDescription(), job.getRequiredSkills());

        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .resume(resume)
                .matchScore(match.score())
                .matchSummary(match.summary())
                .matchedSkills(match.matchedSkills())
                .missingSkills(match.missingSkills())
                .status(Application.Status.APPLIED)
                .build();

        return ApplicationResponse.from(applicationRepository.save(application));
    }

    public List<ApplicationResponse> myApplications(User candidate) {
        return applicationRepository.findByCandidate(candidate).stream()
                .map(ApplicationResponse::from).toList();
    }

    /** Recruiter view: applicants for a job, ranked by AI match score descending. */
    public List<ApplicationResponse> applicantsForJob(Long jobId, User recruiter) {
        Job job = jobService.getJobOrThrow(jobId);
        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new ForbiddenException("You can only view applicants for your own job postings");
        }
        return applicationRepository.findByJobOrderByMatchScoreDesc(job).stream()
                .map(ApplicationResponse::from).toList();
    }

    public ApplicationResponse updateStatus(Long applicationId, Application.Status status, User recruiter) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new com.jobportal.exception.ResourceNotFoundException("Application not found"));
        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new ForbiddenException("You can only update applications for your own job postings");
        }
        application.setStatus(status);
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    /** Candidate withdraws their own application. */
    public void withdraw(Long applicationId, User candidate) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new com.jobportal.exception.ResourceNotFoundException("Application not found"));
        if (!application.getCandidate().getId().equals(candidate.getId())) {
            throw new ForbiddenException("You can only withdraw your own applications");
        }
        applicationRepository.delete(application);
    }
}
