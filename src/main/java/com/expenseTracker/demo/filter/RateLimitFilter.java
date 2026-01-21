package com.expenseTracker.demo.filter;

import com.expenseTracker.demo.exception.RateLimitExceededException;
import com.expenseTracker.demo.util.Constants;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/analytics")) {
            String key = getClientKey(request);
            Bucket bucket = resolveBucket(key, Constants.RateLimit.ANALYTICS_REQUESTS_PER_MINUTE);

            if (!bucket.tryConsume(1)) {
                throw new RateLimitExceededException(Constants.ErrorMessages.RATE_LIMIT_EXCEEDED);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientKey(HttpServletRequest request) {
        String userId = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
        return userId + ":" + request.getRequestURI();
    }

    private Bucket resolveBucket(String key, int requestsPerMinute) {
        return cache.computeIfAbsent(key, k -> createBucket(requestsPerMinute));
    }

    private Bucket createBucket(int requestsPerMinute) {
        Bandwidth limit = Bandwidth.classic(
                requestsPerMinute,
                Refill.intervally(requestsPerMinute, Duration.ofMinutes(1))
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
