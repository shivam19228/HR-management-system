package com.hcl.hrgenius;

import com.hcl.hrgenius.dto.user.LoginRequest;
import com.hcl.hrgenius.dto.user.RegisterRequest;
import com.hcl.hrgenius.dto.user.UserResponse;
import com.hcl.hrgenius.entity.User;
import com.hcl.hrgenius.exception.UnauthorizedException;
import com.hcl.hrgenius.repository.UserRepository;
import com.hcl.hrgenius.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void testRegister_DisallowsAdminRole() {
        RegisterRequest request = new RegisterRequest("Test User", "testuser", "test@hrgenius.com", "Secret123", "Admin");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertTrue(ex.getMessage().contains("Admin"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_SuccessWithBCrypt() {
        RegisterRequest request = new RegisterRequest("Sarah Jenkins", "sarah_j", "sarah@hrgenius.com", "PlainPass123", "HR Specialist");

        when(userRepository.existsByUsername("sarah_j")).thenReturn(false);
        when(userRepository.existsByEmail("sarah@hrgenius.com")).thenReturn(false);
        when(passwordEncoder.encode("PlainPass123")).thenReturn("$2a$10$hashedPasswordSample");

        User savedUser = new User("sarah_j", "$2a$10$hashedPasswordSample", "sarah@hrgenius.com", "Sarah Jenkins", "HR Specialist", "Active");
        savedUser.setId(10L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("sarah_j", response.getUsername());
        assertEquals("HR Specialist", response.getRole());
        verify(passwordEncoder, times(1)).encode("PlainPass123");
    }

    @Test
    void testLogin_InvalidPasswordThrowsUnauthorized() {
        LoginRequest request = new LoginRequest("admin", "wrongPassword");
        User user = new User("admin", "$2a$10$hashedPasswordSample", "admin@hrgenius.com", "Admin User", "Admin", "Active");

        when(userRepository.findByUsernameOrEmail("admin", "admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedPasswordSample")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("admin", "correctPassword");
        User user = new User("admin", "$2a$10$hashedPasswordSample", "admin@hrgenius.com", "Admin User", "Admin", "Active");
        user.setId(1L);

        when(userRepository.findByUsernameOrEmail("admin", "admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "$2a$10$hashedPasswordSample")).thenReturn(true);

        UserResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("admin", response.getUsername());
        assertEquals("Admin", response.getRole());
    }
}
