package com.zhousheng.llcb.controller;

import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.dto.AuthDtos;
import com.zhousheng.llcb.entity.SysUser;
import com.zhousheng.llcb.security.SecurityProperties;
import com.zhousheng.llcb.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityProperties securityProperties;

    public AuthController(AuthService authService, SecurityProperties securityProperties) {
        this.authService = authService;
        this.securityProperties = securityProperties;
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        SysUser user = authService.register(request);
        return ApiResponse.ok(Map.of("userId", user.getId(), "username", user.getUsername()));
    }

    @PostMapping("/login/password")
    public ApiResponse<AuthDtos.AuthResponse> passwordLogin(@Valid @RequestBody AuthDtos.PasswordLoginRequest request,
                                                            HttpServletRequest servletRequest) {
        return ApiResponse.ok(authService.passwordLogin(request, RequestUtils.clientIp(servletRequest)));
    }

    @PostMapping("/login/sms")
    public ApiResponse<AuthDtos.AuthResponse> smsLogin(@Valid @RequestBody AuthDtos.SmsLoginRequest request,
                                                       HttpServletRequest servletRequest) {
        return ApiResponse.ok(authService.smsLogin(request, RequestUtils.clientIp(servletRequest)));
    }

    @PostMapping("/sms-code")
    public ApiResponse<Map<String, String>> issueSmsCode(@Valid @RequestBody AuthDtos.SmsCodeRequest request,
                                                         HttpServletRequest servletRequest) {
        String code = authService.issueSmsCode(request, RequestUtils.clientIp(servletRequest));
        Map<String, String> result = new LinkedHashMap<>();
        result.put("message", "verification code issued");
        if (securityProperties.exposeDevSmsCode()) {
            result.put("devCode", code);
        }
        return ApiResponse.ok(result);
    }

    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok();
    }
}
