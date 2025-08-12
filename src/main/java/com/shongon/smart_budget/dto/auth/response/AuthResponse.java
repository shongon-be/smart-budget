package com.shongon.smart_budget.dto.auth.response;

import com.shongon.smart_budget.model.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthResponse {
    String accessToken;
    String refreshToken;
    String tokenType;
    UserInfo user;

    public AuthResponse(String accessToken, String refreshToken, UserInfo user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserInfo {
        String id;
        String username;
        String email;

        public static UserInfo from(User user) {
            return new UserInfo(
                    user.getId().toString(),
                    user.getUsername(),
                    user.getEmail()
            );
        }
    }
}
