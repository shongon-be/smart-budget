package com.shongon.smart_budget.service.impl;

import com.shongon.smart_budget.exception.AppException;
import com.shongon.smart_budget.exception.ErrorCode;
import com.shongon.smart_budget.dto.auth.request.LoginRequest;
import com.shongon.smart_budget.dto.auth.request.RefreshTokenRequest;
import com.shongon.smart_budget.dto.auth.request.RegisterRequest;
import com.shongon.smart_budget.dto.auth.response.AuthResponse;
import com.shongon.smart_budget.model.User;
import com.shongon.smart_budget.repository.UserRepository;
import com.shongon.smart_budget.service.AuthService;
import com.shongon.smart_budget.utils.JwtUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    JwtUtil jwtUtil;
    PasswordEncoder passwordEncoder;
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        log.info("New user registered: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new AppException(ErrorCode.INVALID_PASSWORD);

        log.info("User logged in: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getToken();

        // Validate refresh token
        if (jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken))
            throw new AppException(ErrorCode.INVALID_TOKEN);

        // Check if token is blacklisted
        String blacklistKey = "blacklist:token:" + refreshToken;
        if (redisTemplate.hasKey(blacklistKey))
            throw new AppException(ErrorCode.TOKEN_EXPIRED);

        String userId = jwtUtil.extractUserId(refreshToken);
        User user = userRepository.findById(new ObjectId(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(user.getId().toString(), user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId().toString(), user.getEmail());


        // Blacklist old refresh token
        redisTemplate.opsForValue().set(blacklistKey, "blacklisted", 7, TimeUnit.DAYS);

        log.info("Token refreshed for user: {}", user.getEmail());

        return new AuthResponse(newAccessToken, newRefreshToken, AuthResponse.UserInfo.from(user));
    }

    @Override
    public void logout(String accessToken) {
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        // Blacklist the token
        String blacklistKey = "blacklist:token:" + accessToken;
        long ttl = jwtUtil.extractExpiration(accessToken).getTime() - System.currentTimeMillis();

        if (ttl > 0) {
            redisTemplate.opsForValue().set(blacklistKey, "blacklisted", ttl, TimeUnit.MILLISECONDS);
        }

        String userId = jwtUtil.extractUserId(accessToken);

        // Remove user from cache
        redisTemplate.delete("user:" + userId);

        log.info("User logged out, token blacklisted");
    }

    @Override
    public User getCurrentUser(String userId) {
        String userCacheKey = "user:" + userId;
        User user = (User) redisTemplate.opsForValue().get(userCacheKey);

        if (user == null) {
            user = userRepository.findById(new ObjectId(userId))
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            // Cache for 1 hour
            redisTemplate.opsForValue().set(userCacheKey, user, 1, TimeUnit.HOURS);
        }

        return user;
    }

    @Override
    public AuthResponse handleOAuth2Login(String email, String name) {
        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            // Create new user from OAuth2
            user = User.builder()
                    .username(name.replaceAll("\\s+", "").toLowerCase())
                    .email(email)
                    .password(passwordEncoder.encode("oauth2_user")) // Random password
                    .createdAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);
            log.info("New OAuth2 user created: {}", email);
        } else {
            log.info("OAuth2 user logged in: {}", email);
        }

        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId().toString(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString(), user.getEmail());

        // Cache user info for 1 hour
        String userCacheKey = "user:" + user.getId().toString();
        redisTemplate.opsForValue().set(
                userCacheKey,
                user,
                1,
                TimeUnit.HOURS
        );

        return new AuthResponse(accessToken, refreshToken, AuthResponse.UserInfo.from(user));
    }
}
