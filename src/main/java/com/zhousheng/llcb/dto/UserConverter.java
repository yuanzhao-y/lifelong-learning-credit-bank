package com.zhousheng.llcb.dto;

import com.zhousheng.llcb.entity.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserConverter {

    @Mapping(target = "idCardMasked", ignore = true)
    @Mapping(target = "phoneMasked", ignore = true)
    @Mapping(target = "roles", ignore = true)
    UserDtos.UserProfileResponse toProfile(SysUser user);
}
