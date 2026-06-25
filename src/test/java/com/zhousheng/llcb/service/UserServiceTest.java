package com.zhousheng.llcb.service;

import com.zhousheng.llcb.TestMybatis;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.dto.UserDtos;
import com.zhousheng.llcb.entity.SysUser;
import com.zhousheng.llcb.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @BeforeAll
    static void metadata() {
        TestMybatis.initialize(SysUser.class);
    }

    @Mock private SysUserMapper userMapper;
    @Mock private SensitiveDataService sensitiveDataService;
    @Mock private RoleService roleService;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userMapper, sensitiveDataService, roleService);
    }

    @Test
    void profileReturnsMaskedSensitiveDataAndRoles() {
        SysUser user = user();
        when(userMapper.selectById(7L)).thenReturn(user);
        when(sensitiveDataService.decrypt("phone-cipher")).thenReturn("13800138000");
        when(sensitiveDataService.decrypt("id-cipher")).thenReturn("110101199001011234");
        when(sensitiveDataService.maskPhone("13800138000")).thenReturn("138****8000");
        when(sensitiveDataService.maskIdCard("110101199001011234")).thenReturn("110101********1234");
        when(roleService.roleCodes(7L)).thenReturn(List.of("learner"));

        UserDtos.UserProfileResponse result = service.profile(7L);

        assertThat(result.phoneMasked()).isEqualTo("138****8000");
        assertThat(result.idCardMasked()).isEqualTo("110101********1234");
        assertThat(result.roles()).containsExactly("learner");
    }

    @Test
    void profileRejectsMissingUser() {
        when(userMapper.selectById(7L)).thenReturn(null);

        assertThatThrownBy(() -> service.profile(7L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void updateProfileEncryptsChangedSensitiveData() {
        SysUser user = user();
        when(userMapper.selectById(7L)).thenReturn(user);
        when(sensitiveDataService.hash("13900139000")).thenReturn("new-phone-hash");
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(sensitiveDataService.encrypt("13900139000")).thenReturn("new-phone-cipher");
        when(sensitiveDataService.hash("110101199901011234")).thenReturn("new-id-hash");
        when(sensitiveDataService.encrypt("110101199901011234")).thenReturn("new-id-cipher");
        when(sensitiveDataService.decrypt(any())).thenReturn(null);
        when(roleService.roleCodes(7L)).thenReturn(List.of("learner"));

        service.updateProfile(7L, new UserDtos.UpdateProfileRequest(
                "Updated", "110101199901011234", "13900139000", "qa@example.com",
                9L, "Beijing", "Shanghai"));

        assertThat(user.getPhoneHash()).isEqualTo("new-phone-hash");
        assertThat(user.getIdCardHash()).isEqualTo("new-id-hash");
        verify(userMapper).updateById(user);
    }

    @Test
    void updateProfileRejectsDuplicatePhoneWithoutWriting() {
        SysUser user = user();
        when(userMapper.selectById(7L)).thenReturn(user);
        when(sensitiveDataService.hash("13900139000")).thenReturn("duplicate");
        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.updateProfile(7L, new UserDtos.UpdateProfileRequest(
                "Updated", null, "13900139000", null, null, null, null)))
                .isInstanceOf(BusinessException.class);

        verify(userMapper, never()).updateById(any());
    }

    private SysUser user() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setUsername("qa_user");
        user.setStatus("enabled");
        user.setPhoneCipher("phone-cipher");
        user.setIdCardCipher("id-cipher");
        return user;
    }
}
