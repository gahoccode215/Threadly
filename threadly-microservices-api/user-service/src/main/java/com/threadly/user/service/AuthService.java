package com.threadly.user.service;

import com.threadly.user.constant.RoleConstants;
import com.threadly.user.dto.response.AuthResponse;
import com.threadly.user.dto.request.LoginRequest;
import com.threadly.user.dto.request.RegisterRequest;
import com.threadly.user.entity.Role;
import com.threadly.user.entity.User;
import com.threadly.user.exception.EmailAlreadyExistsException;
import com.threadly.user.exception.InvalidCredentialsException;
import com.threadly.user.exception.RoleNotFoundException;
import com.threadly.user.repository.RoleRepository;
import com.threadly.user.repository.UserRepository;
import com.threadly.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email already registered: " + request.getEmail(),
                    "EMAIL_ALREADY_EXISTS"
            );
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign default USER role
        Role userRole = roleRepository.findByName(RoleConstants.USER)
                .orElseThrow(() -> new RoleNotFoundException(
                        "Default role not found: " + RoleConstants.USER,
                        "ROLE_NOT_FOUND"
                ));

        user.addRole(userRole);
        User savedUser = userRepository.save(user);

        // Generate JWT tokens using new methods
        String accessToken = jwtUtil.generateAccessToken(savedUser.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getEmail());

        // Get role names
        Set<String> roleNames = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        log.info("User registered successfully with ID: {} and email: {}",
                savedUser.getId(), savedUser.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .roles(roleNames)
                .message("Registration successful")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid email or password",
                        "INVALID_CREDENTIALS"
                ));

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password attempt for email: {}", request.getEmail());
            throw new InvalidCredentialsException(
                    "Invalid email or password",
                    "INVALID_CREDENTIALS"
            );
        }

        // Generate JWT tokens using new methods
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // Get role names
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        log.info("User logged in successfully with ID: {} and email: {}",
                user.getId(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .roles(roleNames)
                .message("Login successful")
                .build();
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Token refresh attempt");

        // Validate refresh token
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new InvalidCredentialsException(
                    "Invalid or expired refresh token",
                    "INVALID_REFRESH_TOKEN"
            );
        }

        // Extract email from refresh token
        String email = jwtUtil.getEmailFromToken(refreshToken);

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException(
                        "User not found for refresh token",
                        "USER_NOT_FOUND"
                ));

        // Generate new access token (keep same refresh token)
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail());

        // Get role names
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        log.info("Token refreshed successfully for user ID: {}", user.getId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Keep same refresh token
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .roles(roleNames)
                .message("Token refreshed successfully")
                .build();
    }


    /**
     * Logout - invalidate token (for future blacklist implementation)
     */
    public void logout(String accessToken) {
        log.info("Logout attempt");

        // Validate token exists and is valid
        if (jwtUtil.validateAccessToken(accessToken)) {
            String email = jwtUtil.getEmailFromToken(accessToken);
            log.info("User logged out: {}", email);

            // TODO: Implement token blacklist in Redis/Database
            // For now, client-side token removal is sufficient
        }
    }
}
