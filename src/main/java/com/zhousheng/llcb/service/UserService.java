package com.zhousheng.llcb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.dto.UserDtos;
import com.zhousheng.llcb.entity.SysUser;
import com.zhousheng.llcb.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserService {

    private final SysUserMapper userMapper;
    private final SensitiveDataService sensitiveDataService;
    private final RoleService roleService;

    public UserService(SysUserMapper userMapper, SensitiveDataService sensitiveDataService, RoleService roleService) {
        this.userMapper = userMapper;
        this.sensitiveDataService = sensitiveDataService;
        this.roleService = roleService;
    }

    public UserDtos.UserProfileResponse profile(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toProfile(user);
    }

    @Transactional
    public UserDtos.UserProfileResponse updateProfile(Long userId, UserDtos.UpdateProfileRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.hasText(request.phone())) {
            String phoneHash = sensitiveDataService.hash(request.phone());
            Long duplicated = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhoneHash, phoneHash)
                    .ne(SysUser::getId, userId));
            if (duplicated > 0) {
                throw new BusinessException("手机号已被使用");
            }
            user.setPhoneCipher(sensitiveDataService.encrypt(request.phone()));
            user.setPhoneHash(phoneHash);
        }
        if (StringUtils.hasText(request.idCard())) {
            user.setIdCardCipher(sensitiveDataService.encrypt(request.idCard()));
            user.setIdCardHash(sensitiveDataService.hash(request.idCard()));
        }
        user.setRealName(request.realName());
        user.setEmail(request.email());
        user.setAvatarFileId(request.avatarFileId());
        user.setBirthPlace(request.birthPlace());
        user.setCurrentAddress(request.currentAddress());
        userMapper.updateById(user);
        return toProfile(user);
    }

    private UserDtos.UserProfileResponse toProfile(SysUser user) {
        String phone = sensitiveDataService.decrypt(user.getPhoneCipher());
        String idCard = sensitiveDataService.decrypt(user.getIdCardCipher());
        List<String> roles = roleService.roleCodes(user.getId());
        return new UserDtos.UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                sensitiveDataService.maskIdCard(idCard),
                sensitiveDataService.maskPhone(phone),
                user.getEmail(),
                user.getAvatarFileId(),
                user.getBirthPlace(),
                user.getCurrentAddress(),
                user.getStatus(),
                user.getLastLoginAt(),
                roles);
    }
}
