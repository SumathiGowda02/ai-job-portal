package com.jobportal.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Storage backend for uploaded resume files. Two implementations are provided —
 * LocalStorageService (disk, default) and S3Service (AWS S3) — selected via the
 * storage.provider property. This lets the app run end-to-end without AWS creds
 * for local dev, while using real S3 in production, the same fallback pattern
 * AiMatchService uses for the LLM call.
 */
public interface ResumeStorageService {

    /** Stores the file and returns the key used to retrieve it later. */
    String uploadResume(Long candidateId, MultipartFile file) throws IOException;

    /** Public/accessible URL for a previously stored key. */
    String getPublicUrl(String key);

    /** Deletes a previously stored file. Safe to call even if the file is already gone. */
    void delete(String key) throws IOException;
}
