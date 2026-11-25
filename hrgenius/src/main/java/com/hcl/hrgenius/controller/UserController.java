package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.user.UserRequest;
import com.hcl.hrgenius.dto.user.UserResponse;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final SecurityHelper securityHelper;
    private final com.hcl.hrgenius.service.ProfileService profileService;

    public UserController(UserService userService, SecurityHelper securityHelper, com.hcl.hrgenius.service.ProfileService profileService) {
        this.userService = userService;
        this.securityHelper = securityHelper;
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public ResponseEntity<com.hcl.hrgenius.dto.user.ProfileResponse> getProfile(HttpServletRequest request) {
        return ResponseEntity.ok(profileService.getCurrentUserProfile(request));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(HttpServletRequest request) {
        securityHelper.requireAdmin(request);
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id, HttpServletRequest request) {
        securityHelper.requireAdmin(request);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest, HttpServletRequest request) {
        securityHelper.requireAdmin(request);
        UserResponse created = userService.createUser(userRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest,
            HttpServletRequest request) {
        securityHelper.requireAdmin(request);
        UserResponse updated = userService.updateUser(id, userRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        securityHelper.requireAdmin(request);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
