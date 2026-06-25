package com.zhousheng.llcb.service;

import com.zhousheng.llcb.TestMybatis;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.entity.SysSmsCode;
import com.zhousheng.llcb.dto.AuthDtos;
import com.zhousheng.llcb.entity.SysRole;
import com.zhousheng.llcb.entity.SysUser;
import com.zhousheng.llcb.mapper.SysRoleMapper;
import com.zhousheng.llcb.mapper.SysSmsCodeMapper;
import com.zhousheng.llcb.mapper.SysUserMapper;
import com.zhousheng.llcb.security.JwtService;
import com.zhousheng.llcb.security.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @BeforeAll
    static void metadata() {
        TestMybatis.initialize(SysUser.class, SysRole.class, SysSmsCode.class);
    }

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysSmsCodeMapper smsCodeMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RoleService roleService;
    @Mock
    private CreditService creditService;
    @Mock
    private SensitiveDataService sensitiveDataService;
    @Mock
    private LoginAttemptService loginAttemptService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userMapper, roleMapper, smsCodeMapper, passwordEncoder,
                jwtService, roleService, creditService, sensitiveDataService, loginAttemptService);
    }

    @Test
    void registerHashesPasswordEncryptsSensitiveDataAndAssignsLearnerRole() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(sensitiveDataService.hash("13800138000")).thenReturn("phone-hash");
        when(sensitiveDataService.encrypt("13800138000")).thenReturn("phone-cipher");
        when(sensitiveDataService.hash("110101199001011234")).thenReturn("id-hash");
        when(sensitiveDataService.encrypt("110101199001011234")).thenReturn("id-cipher");
        when(passwordEncoder.encode("Admin@123456")).thenReturn("bcrypt-hash");
        SysRole learner = new SysRole();
        learner.setId(2L);
        learner.setRoleCode(Constants.ROLE_LEARNER);
        learner.setStatus(Constants.STATUS_ENABLED);
        when(roleMapper.selectOne(any())).thenReturn(learner);
        when(userMapper.insert(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(7L);
            return 1;
        });

        SysUser user = service.register(new AuthDtos.RegisterRequest(
                "qa_learner", "Admin@123456", "Learner", "13800138000",
                "qa@example.com", "110101199001011234", "Beijing", "Beijing"));

        assertThat(user.getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(user.getPhoneCipher()).isEqualTo("phone-cipher");
        assertThat(user.getPhoneHash()).isEqualTo("phone-hash");
        assertThat(user.getStatus()).isEqualTo(Constants.STATUS_ENABLED);
        verify(roleService).assignUserRoles(7L, List.of(2L));
        verify(creditService).getOrCreateAccount(7L);
    }

    @Test
    void passwordLoginReturnsTokenForValidCredentials() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setUsername("qa_learner");
        user.setPasswordHash("bcrypt-hash");
        user.setStatus(Constants.STATUS_ENABLED);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("Admin@123456", "bcrypt-hash")).thenReturn(true);
        when(roleService.roleCodes(7L)).thenReturn(List.of(Constants.ROLE_LEARNER));
        when(jwtService.createToken(eq(7L), eq("qa_learner"), any(Map.class))).thenReturn("token");

        AuthDtos.AuthResponse response = service.passwordLogin(
                new AuthDtos.PasswordLoginRequest("qa_learner", "Admin@123456"), "127.0.0.1");

        assertThat(response.token()).isEqualTo("token");
        assertThat(response.roles()).containsExactly(Constants.ROLE_LEARNER);
        verify(userMapper).updateById(user);
    }

    @Test
    void passwordLoginRejectsInvalidCredentials() {
        when(userMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> service.passwordLogin(
                new AuthDtos.PasswordLoginRequest("qa_learner", "bad-password"), "127.0.0.1"))
                .isInstanceOf(BusinessException.class);
        verify(loginAttemptService).recordFailure("qa_learner", "127.0.0.1");
    }

    @Test
    void registerRejectsWeakPassword() {
        assertThatThrownBy(() -> service.register(new AuthDtos.RegisterRequest(
                "qa_weak", "password", "Weak", "13800138001",
                null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("8-64");
    }

    @Test
    void issueSmsCodeRejectsRecentRequest() {
        when(sensitiveDataService.hash("13800138000")).thenReturn("phone-hash");
        when(smsCodeMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.issueSmsCode(
                new AuthDtos.SmsCodeRequest("13800138000", "login"), "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(429);
    }

    @Test
    void registerRejectsDuplicatePhone() {
        when(userMapper.selectCount(any())).thenReturn(0L, 1L);
        when(sensitiveDataService.hash("13800138000")).thenReturn("phone-hash");

        assertThatThrownBy(() -> service.register(new AuthDtos.RegisterRequest(
                "qa_duplicate", "Admin@123456", "Duplicate", "13800138000",
                null, null, null, null)))
                .isInstanceOf(BusinessException.class);

        verify(userMapper, never()).insert(any());
    }

    @Test
    void passwordLoginRejectsDisabledAccount() {
        SysUser user = new SysUser();
        user.setUsername("qa_disabled");
        user.setPasswordHash("hash");
        user.setStatus(Constants.STATUS_DISABLED);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("Admin@123456", "hash")).thenReturn(true);

        assertThatThrownBy(() -> service.passwordLogin(
                new AuthDtos.PasswordLoginRequest("qa_disabled", "Admin@123456"), "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("状态");
    }

    @Test
    void issueSmsCodeRejectsIpBurst() {
        when(sensitiveDataService.hash("13800138000")).thenReturn("phone-hash");
        when(smsCodeMapper.selectCount(any())).thenReturn(0L, 5L);

        assertThatThrownBy(() -> service.issueSmsCode(
                new AuthDtos.SmsCodeRequest("13800138000", "login"), "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(429);
    }

    @Test
    void validateSmsCodeRejectsMissingOrExpiredCode() {
        when(sensitiveDataService.hash("13800138000")).thenReturn("phone-hash");
        when(smsCodeMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> service.validateSmsCode("13800138000", "login", "123456"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void validateSmsCodeRejectsAfterFiveFailedAttempts() {
        when(sensitiveDataService.hash("13800138000")).thenReturn("phone-hash");
        SysSmsCode sms = smsCode(5);
        when(smsCodeMapper.selectOne(any())).thenReturn(sms);

        assertThatThrownBy(() -> service.validateSmsCode("13800138000", "login", "123456"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(429);
    }

    @Test
    void validateSmsCodeIncrementsFailureCounterForWrongCode() {
        when(sensitiveDataService.hash("13800138000")).thenReturn("phone-hash");
        SysSmsCode sms = smsCode(1);
        when(smsCodeMapper.selectOne(any())).thenReturn(sms);
        when(passwordEncoder.matches("wrong", "code-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.validateSmsCode("13800138000", "login", "wrong"))
                .isInstanceOf(BusinessException.class);

        verify(smsCodeMapper).update(eq(null), any());
    }

    @Test
    void validateSmsCodeMarksCorrectCodeUsed() {
        when(sensitiveDataService.hash("13800138000")).thenReturn("phone-hash");
        SysSmsCode sms = smsCode(0);
        when(smsCodeMapper.selectOne(any())).thenReturn(sms);
        when(passwordEncoder.matches("123456", "code-hash")).thenReturn(true);

        service.validateSmsCode("13800138000", "login", "123456");

        verify(smsCodeMapper).update(eq(null), any());
    }

    @Test
    void changePasswordRejectsWrongOldPassword() {
        SysUser user = new SysUser();
        user.setPasswordHash("old-hash");
        when(userMapper.selectById(7L)).thenReturn(user);
        when(passwordEncoder.matches("wrong", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(7L,
                new AuthDtos.ChangePasswordRequest("wrong", "NewPass@123")))
                .isInstanceOf(BusinessException.class);

        verify(userMapper, never()).updateById(any());
    }

    @Test
    void changePasswordAdvancesVersionEvenWithinSameSecond() {
        SysUser user = new SysUser();
        user.setPasswordHash("old-hash");
        user.setPasswordUpdatedAt(LocalDateTime.of(2026, 6, 25, 10, 0, 0));
        when(userMapper.selectById(7L)).thenReturn(user);
        when(passwordEncoder.matches("OldPass@123", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("NewPass@123")).thenReturn("new-hash");

        service.changePassword(7L, new AuthDtos.ChangePasswordRequest("OldPass@123", "NewPass@123"));

        assertThat(user.getPasswordUpdatedAt()).isAfter(LocalDateTime.of(2026, 6, 25, 10, 0, 0));
        verify(userMapper).updateById(user);
    }

    private SysSmsCode smsCode(int failures) {
        SysSmsCode sms = new SysSmsCode();
        sms.setId(1L);
        sms.setCodeHash("code-hash");
        sms.setVerifyFailCount(failures);
        sms.setExpireAt(LocalDateTime.now().plusMinutes(5));
        return sms;
    }
}
