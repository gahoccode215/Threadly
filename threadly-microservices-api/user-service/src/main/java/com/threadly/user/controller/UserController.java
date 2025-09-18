package com.threadly.user.controller;

import com.threadly.common.dto.ApiResponseDTO;
import com.threadly.user.dto.response.UserProfileResponse;
import com.threadly.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService; // ← Add final

    @GetMapping("/profile")
    public ResponseEntity<ApiResponseDTO<UserProfileResponse>> getProfile(
            @RequestHeader("X-User-ID") Long userId, // ← Receive User ID
            @RequestHeader("X-Gateway-Validated") String validated,
            HttpServletRequest request) {

        log.info("Get profile request from Gateway for user ID: {}", userId);

        // Validation
        if (!"true".equals(validated) || userId == null) {
            throw new RuntimeException("Invalid request - missing Gateway validation");
        }

        // Use User ID for lookup
        UserProfileResponse profile = userService.getCurrentUserProfileById(userId);

        return ResponseEntity.ok(
                ApiResponseDTO.ok(profile, request.getRequestURI(),
                        "Profile retrieved successfully", 200));
    }
}
