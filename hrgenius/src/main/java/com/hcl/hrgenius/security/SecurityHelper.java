package com.hcl.hrgenius.security;

import com.hcl.hrgenius.entity.Employee;
import com.hcl.hrgenius.entity.User;
import com.hcl.hrgenius.exception.ForbiddenException;
import com.hcl.hrgenius.exception.UnauthorizedException;
import com.hcl.hrgenius.repository.EmployeeRepository;
import com.hcl.hrgenius.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityHelper {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final TokenService tokenService;

    @org.springframework.beans.factory.annotation.Autowired
    public SecurityHelper(UserRepository userRepository, EmployeeRepository employeeRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.tokenService = tokenService != null ? tokenService : new TokenService();
    }

    public SecurityHelper(UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.tokenService = new TokenService();
    }

    public User getAuthenticatedUser(HttpServletRequest request) {
        if (request == null) return null;

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            String username = tokenService.validateAndExtractUsername(token);
            if (username != null && !username.isBlank()) {
                // If X-Auth-Username is supplied, verify it matches the cryptographically verified token subject
                String xAuthUser = request.getHeader("X-Auth-Username");
                if (xAuthUser != null && !xAuthUser.isBlank()) {
                    if (!xAuthUser.trim().equalsIgnoreCase(username.trim())) {
                        return null; // Reject impersonation attempt
                    }
                }
                return userRepository.findByUsernameOrEmail(username, username).orElse(null);
            }
            return null; // Token was invalid, expired, or tampered with
        }

        // Check if session has genuinely authenticated user identity
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            Object sessionUsername = session.getAttribute("AUTH_USER_NAME");
            if (sessionUsername instanceof String && !((String) sessionUsername).isBlank()) {
                String sUser = (String) sessionUsername;
                String xAuthUser = request.getHeader("X-Auth-Username");
                if (xAuthUser != null && !xAuthUser.isBlank()) {
                    if (!xAuthUser.trim().equalsIgnoreCase(sUser.trim())) {
                        return null; // Reject impersonation attempt
                    }
                }
                return userRepository.findByUsernameOrEmail(sUser, sUser).orElse(null);
            }
        }

        // An unauthenticated request without a valid bearer token or verified session is rejected.
        // X-Auth-Username is never accepted as standalone proof of identity.
        return null;
    }

    public String normalizeRole(String role) {
        if (role == null) return "EMPLOYEE";
        String r = role.trim().toUpperCase();
        if (r.contains("ADMIN")) return "ADMIN";
        if (r.contains("MANAGE") || r.contains("RECRUIT") || r.contains("LEAD")) return "HR_MANAGER";
        return "EMPLOYEE";
    }

    public void requireAdmin(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            throw new UnauthorizedException("Authentication required. Please log in.");
        }
        String role = normalizeRole(user.getRole());
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Access denied: Administrative privileges required.");
        }
    }

    public void requireAdminOrManager(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            throw new UnauthorizedException("Authentication required. Please log in.");
        }
        String role = normalizeRole(user.getRole());
        if (!"ADMIN".equals(role) && !"HR_MANAGER".equals(role)) {
            throw new ForbiddenException("Access denied: Manager or Administrator privileges required.");
        }
    }

    public boolean isEmployeeOnly(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) return false;
        return "EMPLOYEE".equals(normalizeRole(user.getRole()));
    }

    public Optional<Employee> getAssociatedEmployee(User user) {
        if (user == null) return Optional.empty();
        Optional<Employee> byEmail = employeeRepository.findByEmailIgnoreCase(user.getEmail());
        if (byEmail.isPresent()) return byEmail;
        if (user.getEmployee() != null && !user.getEmployee().isBlank()) {
            return employeeRepository.findByNameIgnoreCase(user.getEmployee().trim());
        }
        return Optional.empty();
    }

    public Optional<Employee> getEmployeeForRequest(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        return getAssociatedEmployee(user);
    }
}
