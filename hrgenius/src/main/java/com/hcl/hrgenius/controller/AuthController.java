package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.user.LoginRequest;
import com.hcl.hrgenius.dto.user.RegisterRequest;
import com.hcl.hrgenius.dto.user.UserResponse;
import com.hcl.hrgenius.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final com.hcl.hrgenius.service.ProfileService profileService;

    public AuthController(AuthService authService, com.hcl.hrgenius.service.ProfileService profileService) {
        this.authService = authService;
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public ResponseEntity<com.hcl.hrgenius.dto.user.ProfileResponse> getProfile(jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok(profileService.getCurrentUserProfile(request));
    }

    @GetMapping("/me")
    public ResponseEntity<com.hcl.hrgenius.dto.user.ProfileResponse> getMe(jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok(profileService.getCurrentUserProfile(request));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        UserResponse response = authService.login(request);
        if (httpRequest != null) {
            jakarta.servlet.http.HttpSession session = httpRequest.getSession(true);
            session.setAttribute("AUTH_USER_NAME", response.getUsername());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<java.util.Map<String, String>> logout(jakarta.servlet.http.HttpServletRequest request) {
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(java.util.Map.of("message", "Logged out successfully"));
    }
}
