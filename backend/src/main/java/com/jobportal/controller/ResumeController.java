package com.jobportal.controller;

import com.jobportal.dto.ResumeResponse;
import com.jobportal.service.CustomUserDetails;
import com.jobportal.service.LocalStorageService;
import com.jobportal.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    // Only present when storage.provider=local (see LocalStorageService's @ConditionalOnProperty).
    // In S3 mode, resume files are served directly from the s3Url on ResumeResponse instead.
    @Autowired(required = false)
    private LocalStorageService storageService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ResumeResponse> upload(@RequestParam("file") MultipartFile file,
                                          @AuthenticationPrincipal CustomUserDetails principal) throws IOException {
        return ResponseEntity.ok(ResumeResponse.from(resumeService.uploadResume(principal.getUser(), file)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<ResumeResponse>> myResumes(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(resumeService.myResumes(principal.getUser()).stream()
                .map(ResumeResponse::from).toList());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        resumeService.deleteResume(id, principal.getUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/file/{candidateId}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable Long candidateId, @PathVariable String filename)
            throws MalformedURLException {
        if (storageService == null) {
            // storage.provider=s3 — nothing to serve locally, client should use the resume's s3Url
            return ResponseEntity.notFound().build();
        }
        Path path = storageService.resolvePath(candidateId + "/" + filename);
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        MediaType contentType = filename.toLowerCase().endsWith(".pdf")
                ? MediaType.APPLICATION_PDF
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
