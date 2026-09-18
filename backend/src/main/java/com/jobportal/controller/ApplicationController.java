package com.jobportal.controller;

import com.jobportal.dto.ApplicationResponse;
import com.jobportal.model.Application;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApplicationResponse> apply(@RequestParam Long jobId, @RequestParam Long resumeId,
                                                       @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(applicationService.apply(jobId, resumeId, principal.getUser()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<ApplicationResponse>> myApplications(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(applicationService.myApplications(principal.getUser()));
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<ApplicationResponse>> applicantsForJob(@PathVariable Long jobId,
                                                                       @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(applicationService.applicantsForJob(jobId, principal.getUser()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApplicationResponse> updateStatus(@PathVariable Long id, @RequestParam Application.Status status,
                                                              @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(applicationService.updateStatus(id, status, principal.getUser()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> withdraw(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        applicationService.withdraw(id, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
