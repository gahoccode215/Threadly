package com.threadly.gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final List<String> OPEN_ENDPOINTS = Arrays.asList(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        log.info("Gateway request: {} {}", method, path); // ← Change to INFO

        // Skip authentication cho open endpoints
        if (isOpenEndpoint(path)) {
            log.info("Open endpoint accessed: {}", path); // ← Change to INFO
            return chain.filter(exchange);
        }

        log.info("Protected endpoint - checking auth: {}", path);

        // Check Bearer token cho protected endpoints
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for: {}", path);
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        if (token.trim().isEmpty()) {
            log.warn("Empty token for: {}", path);
            return unauthorized(exchange, "Empty authentication token");
        }

        try {
            Claims claims = validateJWT(token);

            // Extract userId from JWT subject (CORRECT WAY)
            String userIdStr = claims.getSubject();
            Long userId = Long.valueOf(userIdStr);

            log.info("Extracted userId: {} from JWT subject", userId);

            // Check token expiration
            if (claims.getExpiration().before(new Date())) {
                log.warn("Expired token for user: {}", userId);
                return unauthorized(exchange, "Token has expired");
            }

            // Enrich request với user info
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-ID", String.valueOf(userId))
                    .header("X-Gateway-Validated", "true")
                    .build();

            log.info("JWT validated successfully for user ID: {}", userId);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.warn("JWT validation failed for {}: {}", path, e.getMessage());
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    private Claims validateJWT(String token) throws JwtException {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isOpenEndpoint(String path) {
        boolean isOpen = OPEN_ENDPOINTS.stream().anyMatch(path::startsWith);
        log.info("Checking endpoint {}: isOpen = {}", path, isOpen); // ← Add debug info
        return isOpen;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String errorBody = """
            {
                "status": 401,
                "success": false,
                "message": "%s",
                "error": "UNAUTHORIZED_ACCESS",
                "timestamp": %d,
                "path": "%s"
            }
            """.formatted(message, System.currentTimeMillis(), exchange.getRequest().getURI().getPath());

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(errorBody.getBytes()))
        );
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

