package com.jobportal.service;

import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ForbiddenException;
import com.jobportal.model.Resume;
import com.jobportal.model.User;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ApplicationRepository applicationRepository;
    private final ResumeStorageService storageService;
    private final ResumeTextExtractor textExtractor;

    public Resume uploadResume(User candidate, MultipartFile file) throws IOException {
        String key = storageService.uploadResume(candidate.getId(), file);
        String url = storageService.getPublicUrl(key);
        String extractedText = textExtractor.extractText(file);

        Resume resume = Resume.builder()
                .candidate(candidate)
                .originalFileName(file.getOriginalFilename())
                .s3Key(key)
                .s3Url(url)
                .extractedText(extractedText)
                .build();

        return resumeRepository.save(resume);
    }

    public List<Resume> myResumes(User candidate) {
        return resumeRepository.findByCandidateOrderByUploadedAtDesc(candidate);
    }

    public Resume getResumeOrThrow(Long id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new com.jobportal.exception.ResourceNotFoundException("Resume not found: " + id));
    }

    /** Deletes a resume the candidate owns, as long as it isn't attached to an existing application. */
    public void deleteResume(Long id, User candidate) {
        Resume resume = getResumeOrThrow(id);
        if (!resume.getCandidate().getId().equals(candidate.getId())) {
            throw new ForbiddenException("You can only delete your own resumes");
        }
        if (applicationRepository.existsByResume(resume)) {
            throw new BadRequestException("This resume is attached to an application — withdraw the application first");
        }
        try {
            storageService.delete(resume.getS3Key());
        } catch (IOException e) {
            // Non-fatal: still remove the DB record even if the file is already gone
        }
        resumeRepository.delete(resume);
    }
}