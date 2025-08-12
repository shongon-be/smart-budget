package com.shongon.smart_budget.config;

import com.shongon.smart_budget.model.User;
import com.shongon.smart_budget.repository.UserRepository;
import com.shongon.smart_budget.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userId;

        // Skip if no Authorization header or doesn't start with Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            // Validate token
            if (jwtUtil.validateToken(jwt) || !jwtUtil.isAccessToken(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            userId = jwtUtil.extractUserId(jwt);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Check if the token is blacklisted in Redis
                String blacklistKey = "blacklist:token:" + jwt;
                if (redisTemplate.hasKey(blacklistKey)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // Try to get user from cache first
                String userCacheKey = "user:" + userId;
                User user = (User) redisTemplate.opsForValue().get(userCacheKey);

                if (user == null) {
                    // If not in cache, get from DB and cache it
                    Optional<User> userOpt = userRepository.findById(new ObjectId(userId));
                    if (userOpt.isEmpty()) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    user = userOpt.get();
                    // Cache for 1 hour
                    redisTemplate.opsForValue().set(userCacheKey, user, 1, TimeUnit.HOURS);
                }

                // Create authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            log.error("JWT Authentication failed: {}", e.getMessage());
        }
    }
}
