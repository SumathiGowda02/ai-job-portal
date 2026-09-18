package com.jobportal.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class ResumeTextExtractor {

    /**
     * Extracts plain text from an uploaded PDF resume.
     * Falls back to an empty string for non-PDF files (e.g. .docx) —
     * plug in Apache POI here if you need Word doc support too.
     */
    public String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!filename.endsWith(".pdf")) {
            return "";
        }
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            // Cap length to keep DB column and AI prompt size sane
            return text.length() > 18000 ? text.substring(0, 18000) : text;
        }
    }
}
