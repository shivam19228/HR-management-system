package com.hcl.hrgenius;

import com.hcl.hrgenius.entity.User;
import com.hcl.hrgenius.exception.ForbiddenException;
import com.hcl.hrgenius.exception.UnauthorizedException;
import com.hcl.hrgenius.repository.EmployeeRepository;
import com.hcl.hrgenius.repository.UserRepository;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityHelperTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private TokenService tokenService;
    private SecurityHelper securityHelper;

    private User adminUser;
    private User employeeUser;
    private User managerUser;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        securityHelper = new SecurityHelper(userRepository, employeeRepository, tokenService);

        adminUser = new User("admin", "hashedPass", "admin@hrgenius.com", "Sarah Jenkins", "Admin", "Active");
        adminUser.setId(1L);

        managerUser = new User("david_lead", "hashedPass", "david@hrgenius.com", "David Chen", "HR Manager", "Active");
        managerUser.setId(2L);

        employeeUser = new User("jessica_dev", "hashedPass", "jessica@hrgenius.com", "Jessica Taylor", "Employee", "Active");
        employeeUser.setId(3L);
    }

    @Test
    void testUnauthenticatedRequest_ReturnsNull() {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getSession(false)).thenReturn(null);

        User result = securityHelper.getAuthenticatedUser(request);
        assertNull(result, "Unauthenticated request without token or session must return null");
    }

    @Test
    void testSpoofedXAuthUsernameWithoutToken_ReturnsNull() {
        // SECURITY TEST: An unauthenticated caller sending X-Auth-Username header alone must NEVER be authenticated
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getSession(false)).thenReturn(null);

        User result = securityHelper.getAuthenticatedUser(request);
        assertNull(result, "Spoofed X-Auth-Username without valid bearer token or session MUST be rejected");
    }

    @Test
    void testValidBearerToken_ReturnsUser() {
        String token = tokenService.generateToken(adminUser);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getHeader("X-Auth-Username")).thenReturn(null);
        when(userRepository.findByUsernameOrEmail("admin", "admin")).thenReturn(Optional.of(adminUser));

        User result = securityHelper.getAuthenticatedUser(request);
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("Admin", result.getRole());
    }

    @Test
    void testMismatchedXAuthUsernameAndToken_ReturnsNull() {
        // SECURITY TEST: Token belongs to jessica_dev, but header claims to be admin
        String token = tokenService.generateToken(employeeUser);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getHeader("X-Auth-Username")).thenReturn("admin");

        User result = securityHelper.getAuthenticatedUser(request);
        assertNull(result, "Mismatched token subject and X-Auth-Username must reject request as spoof attempt");
    }

    @Test
    void testForgedTokenSignature_ReturnsNull() {
        // Manipulate token signature
        String validToken = tokenService.generateToken(adminUser);
        String forgedToken = validToken.substring(0, validToken.length() - 5) + "abcde";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + forgedToken);

        User result = securityHelper.getAuthenticatedUser(request);
        assertNull(result, "Forged token signature must fail cryptographic verification");
    }

    @Test
    void testRequireAdmin_Unauthenticated_ThrowsUnauthorized() {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getSession(false)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> securityHelper.requireAdmin(request));
    }

    @Test
    void testRequireAdmin_EmployeeRole_ThrowsForbidden() {
        String token = tokenService.generateToken(employeeUser);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(userRepository.findByUsernameOrEmail("jessica_dev", "jessica_dev")).thenReturn(Optional.of(employeeUser));

        assertThrows(ForbiddenException.class, () -> securityHelper.requireAdmin(request));
    }

    @Test
    void testRequireAdmin_AdminRole_Succeeds() {
        String token = tokenService.generateToken(adminUser);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(userRepository.findByUsernameOrEmail("admin", "admin")).thenReturn(Optional.of(adminUser));

        assertDoesNotThrow(() -> securityHelper.requireAdmin(request));
    }

    @Test
    void testRequireAdminOrManager_EmployeeRole_ThrowsForbidden() {
        String token = tokenService.generateToken(employeeUser);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(userRepository.findByUsernameOrEmail("jessica_dev", "jessica_dev")).thenReturn(Optional.of(employeeUser));

        assertThrows(ForbiddenException.class, () -> securityHelper.requireAdminOrManager(request));
    }

    @Test
    void testRequireAdminOrManager_ManagerRole_Succeeds() {
        String token = tokenService.generateToken(managerUser);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(userRepository.findByUsernameOrEmail("david_lead", "david_lead")).thenReturn(Optional.of(managerUser));

        assertDoesNotThrow(() -> securityHelper.requireAdminOrManager(request));
    }
}
