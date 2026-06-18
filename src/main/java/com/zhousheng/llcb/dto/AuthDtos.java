package com.zhousheng.llcb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank String username,
            @NotBlank String password,
            String realName,
            String phone,
            String email,
            String idCard,
            String birthPlace,
            String currentAddress) {
    }

    public record PasswordLoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record SmsCodeRequest(@NotBlank String phone, @NotBlank String scene) {
    }

    public record SmsLoginRequest(@NotBlank String phone, @NotBlank String code) {
    }

    public record ResetPasswordRequest(@NotBlank String phone, @NotBlank String code, @NotBlank String newPassword) {
    }

    public record ChangePasswordRequest(@NotBlank String oldPassword, @NotBlank String newPassword) {
    }

    public record AuthResponse(@NotBlank String token, @NotNull Long userId, String username, List<String> roles) {
    }
}
