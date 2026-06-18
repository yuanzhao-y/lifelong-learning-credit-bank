package com.zhousheng.llcb.controller;

import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.dto.AuthDtos;
import com.zhousheng.llcb.entity.SysUser;
import com.zhousheng.llcb.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
        return ApiResponse.ok(Map.of("devCode", code, "message", "开发环境直接返回验证码，生产环境应接入短信网关"));
    }

    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok();
    }
}
