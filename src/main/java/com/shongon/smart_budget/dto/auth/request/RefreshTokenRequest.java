package com.shongon.smart_budget.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshTokenRequest {

    @NotBlank(message = "Token is required")
    String token;
}
