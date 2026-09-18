package com.jobportal.controller;

import com.jobportal.dto.JobRequest;
import com.jobportal.dto.JobResponse;
import com.jobportal.dto.PagedResult;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.service.CustomUserDetails;
import com.jobportal.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest request,
                                               @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(jobService.createJob(request, principal.getUser()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponse> update(@PathVariable Long id, @Valid @RequestBody JobRequest request,
                                               @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(jobService.updateJob(id, request, principal.getUser()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        jobService.deleteJob(id, principal.getUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJob(id));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<JobResponse>> myJobs(@AuthenticationPrincipal CustomUserDetails principal,
                                                     Pageable pageable) {
        return ResponseEntity.ok(jobService.myJobs(principal.getUser(), pageable));
    }

    @GetMapping
    public ResponseEntity<PagedResult<JobResponse>> search(@RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) String location,
                                                        @RequestParam(required = false) Job.JobType jobType,
                                                        Pageable pageable) {
      return ResponseEntity.ok(jobService.search(keyword, location, jobType, pageable));
  }
}
