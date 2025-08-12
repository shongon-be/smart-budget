package com.shongon.smart_budget.controller;

import com.shongon.smart_budget.dto.auth.request.LoginRequest;
import com.shongon.smart_budget.dto.auth.request.RefreshTokenRequest;
import com.shongon.smart_budget.dto.auth.request.RegisterRequest;
import com.shongon.smart_budget.dto.auth.response.AuthResponse;
import com.shongon.smart_budget.model.User;
import com.shongon.smart_budget.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            authService.logout(authHeader);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserInfo> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(AuthResponse.UserInfo.from(user));
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> oauth2Success(@AuthenticationPrincipal OAuth2User oauth2User) {
        try {
            // Extract user info from OAuth2User
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");

            // Handle Google OAuth2 login
            AuthResponse response = authService.handleOAuth2Login(email, name);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null));
        }
    }

    @GetMapping("/oauth2/failure")
    public ResponseEntity<Map<String, String>> oauth2Failure() {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "OAuth2 login failed"));
    }
}
