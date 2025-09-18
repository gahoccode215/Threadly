package com.threadly.user.controller;

import com.threadly.common.dto.ApiResponseDTO;
import com.threadly.user.dto.request.LoginRequest;
import com.threadly.user.dto.request.RegisterRequest;
import com.threadly.user.dto.response.AuthResponse;
import com.threadly.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest) {

        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok(
                        response,
                        servletRequest.getRequestURI(),
                        "User registered successfully",
                        201
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponseDTO.ok(
                        response,
                        servletRequest.getRequestURI(),
                        "User logged in successfully",
                        200
                ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDTO<AuthResponse>> refreshToken(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest servletRequest) {

        // Extract token from "Bearer <token>"
        String refreshToken = authHeader.substring(7);

        AuthResponse response = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(
                ApiResponseDTO.ok(
                        response,
                        servletRequest.getRequestURI(),
                        "Token refreshed successfully",
                        200
                ));
    }


}
