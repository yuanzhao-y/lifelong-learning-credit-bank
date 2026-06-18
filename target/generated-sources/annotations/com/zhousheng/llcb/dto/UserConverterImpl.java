package com.zhousheng.llcb.dto;

import com.zhousheng.llcb.entity.SysUser;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-19T01:09:56+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class UserConverterImpl implements UserConverter {

    @Override
    public UserDtos.UserProfileResponse toProfile(SysUser user) {
        if ( user == null ) {
            return null;
        }

        Long id = null;
        String username = null;
        String realName = null;
        String email = null;
        Long avatarFileId = null;
        String birthPlace = null;
        String currentAddress = null;
        String status = null;
        LocalDateTime lastLoginAt = null;

        id = user.getId();
        username = user.getUsername();
        realName = user.getRealName();
        email = user.getEmail();
        avatarFileId = user.getAvatarFileId();
        birthPlace = user.getBirthPlace();
        currentAddress = user.getCurrentAddress();
        status = user.getStatus();
        lastLoginAt = user.getLastLoginAt();

        String idCardMasked = null;
        String phoneMasked = null;
        List<String> roles = null;

        UserDtos.UserProfileResponse userProfileResponse = new UserDtos.UserProfileResponse( id, username, realName, idCardMasked, phoneMasked, email, avatarFileId, birthPlace, currentAddress, status, lastLoginAt, roles );

        return userProfileResponse;
    }
}
