package com.jobportal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements ResumeStorageService {

    @Value("${storage.local-dir:./uploaded-resumes}")
    private String baseDir;

    @Override
    public String uploadResume(Long candidateId, MultipartFile file) throws IOException {
        Path dir = Paths.get(baseDir, String.valueOf(candidateId));
        Files.createDirectories(dir);

        String safeName = sanitizeFilename(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + "-" + safeName;
        Path target = dir.resolve(storedFilename);
        file.transferTo(target);

        return candidateId + "/" + storedFilename;
    }

    @Override
    public String getPublicUrl(String key) {
        return "/api/resumes/file/" + key;
    }

    @Override
    public void delete(String key) throws IOException {
        Files.deleteIfExists(resolvePath(key));
    }

    public Path resolvePath(String key) {
        return Paths.get(baseDir, key);
    }

    /** Strips characters that break URLs or file paths (brackets, parens, spaces, etc). */
    private String sanitizeFilename(String original) {
        if (original == null || original.isBlank()) {
            return "resume.pdf";
        }
        return original.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
