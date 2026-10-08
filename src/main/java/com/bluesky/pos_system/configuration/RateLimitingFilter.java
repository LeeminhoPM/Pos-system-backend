package com.bluesky.pos_system.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe In-Memory Sliding Window Rate Limiting Filter.
 * Enforces request throttling per client IP address or JWT Token.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 150;
    private static final long WINDOW_MS = 60_000L; // 1 minute

    private final Map<String, ClientWindow> clientWindows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static class ClientWindow {
        long windowStart;
        AtomicInteger requestCount;

        ClientWindow(long start) {
            this.windowStart = start;
            this.requestCount = new AtomicInteger(1);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip rate limiting for static assets, Swagger docs, or H2 console
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/h2-console") ||
               path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientKey = extractClientKey(request);
        long now = System.currentTimeMillis();

        ClientWindow window = clientWindows.compute(clientKey, (key, existing) -> {
            if (existing == null || (now - existing.windowStart) > WINDOW_MS) {
                return new ClientWindow(now);
            }
            existing.requestCount.incrementAndGet();
            return existing;
        });

        int currentCount = window.requestCount.get();
        long resetSeconds = Math.max(0, (WINDOW_MS - (now - window.windowStart)) / 1000);

        // Populate standard rate limit response headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, MAX_REQUESTS_PER_MINUTE - currentCount)));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetSeconds));

        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for client: {}, count: {} in current window", clientKey, currentCount);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> errorBody = Map.of(
                    "success", false,
                    "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                    "errorCode", "RATE_LIMIT_EXCEEDED",
                    "message", "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau " + resetSeconds + " giây.",
                    "timestamp", LocalDateTime.now().toString(),
                    "path", request.getRequestURI()
            );

            objectMapper.writeValue(response.getWriter(), errorBody);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientKey(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return "user:" + authHeader.substring(7, Math.min(27, authHeader.length()));
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return "ip:" + xForwardedFor.split(",")[0].trim();
        }

        return "ip:" + request.getRemoteAddr();
    }
}
