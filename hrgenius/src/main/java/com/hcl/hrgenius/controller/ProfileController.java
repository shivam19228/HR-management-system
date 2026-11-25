package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.user.ProfileResponse;
import com.hcl.hrgenius.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(HttpServletRequest request) {
        ProfileResponse profile = profileService.getCurrentUserProfile(request);
        return ResponseEntity.ok(profile);
    }
}
