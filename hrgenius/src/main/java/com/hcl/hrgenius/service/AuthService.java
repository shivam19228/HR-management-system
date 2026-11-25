package com.hcl.hrgenius.service;

import com.hcl.hrgenius.dto.user.LoginRequest;
import com.hcl.hrgenius.dto.user.RegisterRequest;
import com.hcl.hrgenius.dto.user.UserResponse;
import com.hcl.hrgenius.entity.User;
import com.hcl.hrgenius.exception.UnauthorizedException;
import com.hcl.hrgenius.repository.UserRepository;
import com.hcl.hrgenius.entity.Employee;
import com.hcl.hrgenius.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcl.hrgenius.security.TokenService;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private EmployeeRepository employeeRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.hcl.hrgenius.security.TokenService tokenService = new com.hcl.hrgenius.security.TokenService();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered.");
        }

        // Strict role safety: Disallow public self-registration as Admin
        String requestedRole = request.getRole() != null ? request.getRole().trim() : "Employee";
        if (requestedRole.equalsIgnoreCase("Admin") || requestedRole.toUpperCase().contains("ADMIN")) {
            throw new IllegalArgumentException("Public registration cannot assign the 'Admin' role. Please select a staff role or contact an administrator.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(email);
        user.setEmployee(request.getName().trim());
        user.setRole(requestedRole);
        user.setStatus("Active");

        User saved = userRepository.save(user);

        return mapToUserResponse(saved);
    }

    public UserResponse login(LoginRequest request) {
        String identifier = request.getUsername().trim();
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password.");
        }

        if ("Inactive".equalsIgnoreCase(user.getStatus())) {
            throw new UnauthorizedException("Your account is currently inactive. Please contact an administrator.");
        }

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        String empId = null;
        Long employeeId = null;

        if (employeeRepository != null) {
            Optional<Employee> empOpt = employeeRepository.findByEmailIgnoreCase(user.getEmail());
            if (empOpt.isEmpty() && user.getEmployee() != null && !user.getEmployee().isBlank()) {
                empOpt = employeeRepository.findByNameIgnoreCase(user.getEmployee().trim());
            }
            if (empOpt.isPresent()) {
                empId = empOpt.get().getEmpId();
                employeeId = empOpt.get().getId();
            }
        }

        String token = null;
        if (tokenService != null) {
            token = tokenService.generateToken(user);
        }

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmployee(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                empId,
                employeeId,
                token
        );
    }
}
