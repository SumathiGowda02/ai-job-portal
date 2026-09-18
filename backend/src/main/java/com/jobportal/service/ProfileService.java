package com.jobportal.service;

import com.jobportal.dto.ProfileResponse;
import com.jobportal.dto.UpdateProfileRequest;
import com.jobportal.model.User;
import com.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileResponse getProfile(User user) {
        return ProfileResponse.from(user);
    }

    public ProfileResponse updateProfile(User user, UpdateProfileRequest request) {
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (user.getRole() == com.jobportal.model.Role.RECRUITER) {
            user.setCompanyName(request.getCompanyName());
            user.setCompanyDescription(request.getCompanyDescription());
        } else {
            user.setEducation(request.getEducation());
            user.setSkills(request.getSkills());
        }
        user.setBio(request.getBio());
        return ProfileResponse.from(userRepository.save(user));
    }
}
