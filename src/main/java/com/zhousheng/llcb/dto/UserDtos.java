package com.zhousheng.llcb.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class UserDtos {

    private UserDtos() {
    }

    public record UserProfileResponse(
            Long id,
            String username,
            String realName,
            String idCardMasked,
            String phoneMasked,
            String email,
            Long avatarFileId,
            String birthPlace,
            String currentAddress,
            String status,
            LocalDateTime lastLoginAt,
            List<String> roles) {
    }

    public record UpdateProfileRequest(
            String realName,
            String idCard,
            String phone,
            String email,
            Long avatarFileId,
            String birthPlace,
            String currentAddress) {
    }

    public record UserStatusRequest(String status) {
    }

    public record AssignRolesRequest(List<Long> roleIds) {
    }
}
