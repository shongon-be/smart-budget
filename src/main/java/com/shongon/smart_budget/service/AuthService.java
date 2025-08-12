package com.shongon.smart_budget.service;

import com.shongon.smart_budget.dto.auth.request.LoginRequest;
import com.shongon.smart_budget.dto.auth.request.RefreshTokenRequest;
import com.shongon.smart_budget.dto.auth.request.RegisterRequest;
import com.shongon.smart_budget.dto.auth.response.AuthResponse;
import com.shongon.smart_budget.model.User;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String accessToken);
    User getCurrentUser(String userId);
    AuthResponse handleOAuth2Login(String email, String name);
}
