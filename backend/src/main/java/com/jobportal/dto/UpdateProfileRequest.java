package com.jobportal.dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String companyName;
    private String companyDescription;
    private String education;
    private List<String> skills;
    private String bio;
}
