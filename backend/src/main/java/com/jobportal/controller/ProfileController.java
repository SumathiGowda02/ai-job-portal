package com.jobportal.controller;

import com.jobportal.dto.ProfileResponse;
import com.jobportal.dto.UpdateProfileRequest;
import com.jobportal.service.CustomUserDetails;
import com.jobportal.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> me(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(profileService.getProfile(principal.getUser()));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMe(@RequestBody UpdateProfileRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(profileService.updateProfile(principal.getUser(), request));
    }
}
