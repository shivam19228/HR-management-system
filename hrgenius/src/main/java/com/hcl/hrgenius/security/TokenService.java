
package com.hcl.hrgenius.security;

import com.hcl.hrgenius.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service
public class TokenService {

    private final String secretKey;

    // Constructor: JWT secret environment variable se lega
    public TokenService(
            @Value("${app.security.jwt.secret}") String secretKey) {

        if (secretKey == null || secretKey.isBlank()
                || secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must contain at least 32 bytes");
        }

        this.secretKey = secretKey;
    }

    // Generate authentication token
    public String generateToken(User user) {

        if (user == null || user.getUsername() == null) {
            return null;
        }

        try {
            long now = Instant.now().getEpochSecond();
            long exp = now + (24 * 3600);

            String username = user.getUsername().trim();
            String role = user.getRole() != null
                    ? user.getRole().trim()
                    : "Employee";

            String rawPayload = username + ":" + role + ":" + exp;

            String encodedPayload = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                            rawPayload.getBytes(StandardCharsets.UTF_8));

            String signature = sign(encodedPayload);

            return encodedPayload + "." + signature;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error generating cryptographic authentication token", e);
        }
    }

    // Validate token and extract username
    public String validateAndExtractUsername(String token) {

        if (token == null || token.isBlank()) {
            return null;
        }

        String[] parts = token.trim().split("\\.");

        if (parts.length != 2) {
            return null;
        }

        String encodedPayload = parts[0];
        String actualSignature = parts[1];

        String expectedSignature = sign(encodedPayload);

        // Constant-time signature comparison
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                actualSignature.getBytes(StandardCharsets.UTF_8))) {
            return null;
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encodedPayload);

            String rawPayload = new String(
                    decoded, StandardCharsets.UTF_8);

            String[] segments = rawPayload.split(":");

            if (segments.length != 3) {
                return null;
            }

            String username = segments[0];
            long exp = Long.parseLong(segments[2]);

            if (Instant.now().getEpochSecond() > exp) {
                return null;
            }

            return username;

        } catch (Exception e) {
            return null;
        }
    }

    // Generate HMAC-SHA256 signature
    private String sign(String data) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");

            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(
                    data.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hmacBytes);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error signing token payload", e);
        }
    }
}
