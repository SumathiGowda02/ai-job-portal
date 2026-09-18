package com.jobportal.dto;

import com.jobportal.model.User;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String companyName;
    private String companyDescription;
    private String education;
    private List<String> skills;
    private String bio;
    private LocalDateTime createdAt;

    public static ProfileResponse from(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .companyName(user.getCompanyName())
                .companyDescription(user.getCompanyDescription())
                .education(user.getEducation())
                .skills(user.getSkills())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
