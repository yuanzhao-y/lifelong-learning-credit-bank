package com.zhousheng.llcb.controller;

import com.zhousheng.llcb.dto.AuthDtos;
import com.zhousheng.llcb.entity.SysUser;
import com.zhousheng.llcb.security.SecurityProperties;
import com.zhousheng.llcb.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private AuthController exposingController;
    private AuthController hidingController;

    @BeforeEach
    void setUp() {
        exposingController = new AuthController(authService,
                new SecurityProperties(List.of(), List.of(), true));
        hidingController = new AuthController(authService,
                new SecurityProperties(List.of(), List.of(), false));
    }

    @Test
    void registerReturnsStableIdentityPayload() {
        AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest(
                "qa_user", "QaUser@123456", "QA User", "13000000000",
                null, null, null, null);
        SysUser user = new SysUser();
        user.setId(77L);
        user.setUsername("qa_user");
        when(authService.register(request)).thenReturn(user);

        var response = exposingController.register(request);

        assertThat(response.getData())
                .containsEntry("userId", 77L)
                .containsEntry("username", "qa_user");
    }

    @Test
    void passwordLoginUsesForwardedClientIp() {
        AuthDtos.PasswordLoginRequest request = new AuthDtos.PasswordLoginRequest("qa_user", "QaUser@123456");
        AuthDtos.AuthResponse auth = new AuthDtos.AuthResponse("token", 77L, "qa_user", List.of("learner"));
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader("X-Forwarded-For", "10.0.0.1, 127.0.0.1");
        servletRequest.setRemoteAddr("127.0.0.1");
        when(authService.passwordLogin(request, "10.0.0.1")).thenReturn(auth);

        var response = exposingController.passwordLogin(request, servletRequest);

        assertThat(response.getData()).isSameAs(auth);
    }

    @Test
    void smsCodeExposureFollowsSecurityProperty() {
        AuthDtos.SmsCodeRequest request = new AuthDtos.SmsCodeRequest("13000000000", "login");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRemoteAddr("127.0.0.1");
        when(authService.issueSmsCode(request, "127.0.0.1")).thenReturn("123456");

        assertThat(exposingController.issueSmsCode(request, servletRequest).getData())
                .containsEntry("devCode", "123456");
        assertThat(hidingController.issueSmsCode(request, servletRequest).getData())
                .doesNotContainKey("devCode");
    }

    @Test
    void smsLoginAndPasswordResetDelegateToService() {
        AuthDtos.SmsLoginRequest smsLogin = new AuthDtos.SmsLoginRequest("13000000000", "123456");
        AuthDtos.AuthResponse auth = new AuthDtos.AuthResponse("token", 77L, "qa_user", List.of("learner"));
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRemoteAddr("127.0.0.1");
        when(authService.smsLogin(smsLogin, "127.0.0.1")).thenReturn(auth);

        assertThat(exposingController.smsLogin(smsLogin, servletRequest).getData()).isSameAs(auth);
        assertThat(exposingController.resetPassword(
                new AuthDtos.ResetPasswordRequest("13000000000", "123456", "QaUser@654321")).getCode())
                .isZero();
    }
}
