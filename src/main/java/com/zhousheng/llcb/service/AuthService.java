package com.zhousheng.llcb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.common.Constants;
import com.zhousheng.llcb.dto.AuthDtos;
import com.zhousheng.llcb.entity.SysRole;
import com.zhousheng.llcb.entity.SysSmsCode;
import com.zhousheng.llcb.entity.SysUser;
import com.zhousheng.llcb.mapper.SysRoleMapper;
import com.zhousheng.llcb.mapper.SysSmsCodeMapper;
import com.zhousheng.llcb.mapper.SysUserMapper;
import com.zhousheng.llcb.security.JwtService;
import com.zhousheng.llcb.security.LoginAttemptService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysSmsCodeMapper smsCodeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RoleService roleService;
    private final CreditService creditService;
    private final SensitiveDataService sensitiveDataService;
    private final LoginAttemptService loginAttemptService;

    public AuthService(SysUserMapper userMapper,
                       SysRoleMapper roleMapper,
                       SysSmsCodeMapper smsCodeMapper,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RoleService roleService,
                       CreditService creditService,
                       SensitiveDataService sensitiveDataService,
                       LoginAttemptService loginAttemptService) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.smsCodeMapper = smsCodeMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.roleService = roleService;
        this.creditService = creditService;
        this.sensitiveDataService = sensitiveDataService;
        this.loginAttemptService = loginAttemptService;
    }

    @Transactional
    public SysUser register(AuthDtos.RegisterRequest request) {
        validatePassword(request.password());
        ensureUsernameAvailable(request.username(), null);
        String phoneHash = sensitiveDataService.hash(request.phone());
        if (StringUtils.hasText(phoneHash) && userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhoneHash, phoneHash)) > 0) {
            throw new BusinessException("手机号已注册");
        }
        SysUser user = new SysUser();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRealName(request.realName());
        user.setPhoneCipher(sensitiveDataService.encrypt(request.phone()));
        user.setPhoneHash(phoneHash);
        user.setIdCardCipher(sensitiveDataService.encrypt(request.idCard()));
        user.setIdCardHash(sensitiveDataService.hash(request.idCard()));
        user.setEmail(request.email());
        user.setBirthPlace(request.birthPlace());
        user.setCurrentAddress(request.currentAddress());
        user.setStatus(Constants.STATUS_ENABLED);
        user.setPasswordUpdatedAt(nextPasswordVersion(null));
        userMapper.insert(user);

        SysRole learner = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, Constants.ROLE_LEARNER)
                .last("limit 1"));
        if (learner != null) {
            roleService.assignUserRoles(user.getId(), java.util.List.of(learner.getId()));
        }
        creditService.getOrCreateAccount(user.getId());
        return user;
    }

    public AuthDtos.AuthResponse passwordLogin(AuthDtos.PasswordLoginRequest request, String ip) {
        loginAttemptService.checkAllowed(request.username(), ip);
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.username())
                .last("limit 1"));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttemptService.recordFailure(request.username(), ip);
            throw new BusinessException("账号或密码错误");
        }
        assertEnabled(user);
        loginAttemptService.clear(request.username(), ip);
        return loginSuccess(user, ip);
    }

    public AuthDtos.AuthResponse smsLogin(AuthDtos.SmsLoginRequest request, String ip) {
        validateSmsCode(request.phone(), "login", request.code());
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhoneHash, sensitiveDataService.hash(request.phone()))
                .last("limit 1"));
        if (user == null) {
            throw new BusinessException("手机号未注册");
        }
        assertEnabled(user);
        return loginSuccess(user, ip);
    }

    @Transactional
    public String issueSmsCode(AuthDtos.SmsCodeRequest request, String ip) {
        String phoneHash = sensitiveDataService.hash(request.phone());
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        long recentRequests = smsCodeMapper.selectCount(new LambdaQueryWrapper<SysSmsCode>()
                .eq(SysSmsCode::getPhoneHash, phoneHash)
                .eq(SysSmsCode::getScene, request.scene())
                .ge(SysSmsCode::getCreatedAt, oneMinuteAgo));
        long recentIpRequests = smsCodeMapper.selectCount(new LambdaQueryWrapper<SysSmsCode>()
                .eq(SysSmsCode::getRequestIp, ip)
                .ge(SysSmsCode::getCreatedAt, oneMinuteAgo));
        if (recentRequests > 0 || recentIpRequests >= 5) {
            throw new BusinessException(429, "验证码发送过于频繁，请稍后重试");
        }
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        SysSmsCode smsCode = new SysSmsCode();
        smsCode.setPhoneHash(phoneHash);
        smsCode.setScene(request.scene());
        smsCode.setCodeHash(passwordEncoder.encode(code));
        smsCode.setExpireAt(LocalDateTime.now().plusMinutes(5));
        smsCode.setVerifyFailCount(0);
        smsCode.setRequestIp(ip);
        smsCodeMapper.insert(smsCode);
        return code;
    }

    @Transactional
    public void resetPassword(AuthDtos.ResetPasswordRequest request) {
        validatePassword(request.newPassword());
        validateSmsCode(request.phone(), "reset_password", request.code());
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhoneHash, sensitiveDataService.hash(request.phone()))
                .last("limit 1"));
        if (user == null) {
            throw new BusinessException("手机号未注册");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordUpdatedAt(nextPasswordVersion(user.getPasswordUpdatedAt()));
        userMapper.updateById(user);
    }

    @Transactional
    public void changePassword(Long userId, AuthDtos.ChangePasswordRequest request) {
        validatePassword(request.newPassword());
        SysUser user = userMapper.selectById(userId);
        if (user == null || !passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException("旧密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordUpdatedAt(nextPasswordVersion(user.getPasswordUpdatedAt()));
        userMapper.updateById(user);
    }

    @Transactional
    public void validateSmsCode(String phone, String scene, String code) {
        SysSmsCode smsCode = smsCodeMapper.selectOne(new LambdaQueryWrapper<SysSmsCode>()
                .eq(SysSmsCode::getPhoneHash, sensitiveDataService.hash(phone))
                .eq(SysSmsCode::getScene, scene)
                .isNull(SysSmsCode::getUsedAt)
                .gt(SysSmsCode::getExpireAt, LocalDateTime.now())
                .orderByDesc(SysSmsCode::getCreatedAt)
                .last("limit 1"));
        if (smsCode != null && smsCode.getVerifyFailCount() != null && smsCode.getVerifyFailCount() >= 5) {
            throw new BusinessException(429, "验证码尝试次数过多，请重新获取");
        }
        if (smsCode == null || !passwordEncoder.matches(code, smsCode.getCodeHash())) {
            if (smsCode != null) {
                smsCodeMapper.update(null, new LambdaUpdateWrapper<SysSmsCode>()
                        .eq(SysSmsCode::getId, smsCode.getId())
                        .set(SysSmsCode::getVerifyFailCount, smsCode.getVerifyFailCount() + 1));
            }
            throw new BusinessException("验证码错误或已过期");
        }
        smsCodeMapper.update(null, new LambdaUpdateWrapper<SysSmsCode>()
                .eq(SysSmsCode::getId, smsCode.getId())
                .set(SysSmsCode::getUsedAt, LocalDateTime.now()));
    }

    public void ensureUsernameAvailable(String username, Long excludeId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(SysUser::getId, excludeId);
        }
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("账号已存在");
        }
    }

    private AuthDtos.AuthResponse loginSuccess(SysUser user, String ip) {
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);
        var roles = roleService.roleCodes(user.getId());
        long passwordVersion = user.getPasswordUpdatedAt() == null
                ? 0L
                : user.getPasswordUpdatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String token = jwtService.createToken(user.getId(), user.getUsername(),
                Map.of("roles", roles, "pwdAt", passwordVersion));
        return new AuthDtos.AuthResponse(token, user.getId(), user.getUsername(), roles);
    }

    private void assertEnabled(SysUser user) {
        if (!Constants.STATUS_ENABLED.equals(user.getStatus())) {
            throw new BusinessException("账号状态不可用");
        }
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password)
                || password.length() < 8
                || password.length() > 64
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*[a-z].*")
                || !password.matches(".*\\d.*")
                || !password.matches(".*[^A-Za-z0-9].*")) {
            throw new BusinessException("密码必须为 8-64 位，并包含大小写字母、数字和特殊字符");
        }
    }

    private LocalDateTime nextPasswordVersion(LocalDateTime current) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (current == null) {
            return now;
        }
        LocalDateTime next = current.truncatedTo(ChronoUnit.SECONDS).plusSeconds(1);
        return now.isAfter(next) ? now : next;
    }
}
