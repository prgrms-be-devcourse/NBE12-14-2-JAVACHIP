package com.budzet.domain.user.controller;

import com.budzet.domain.user.dto.*;
import com.budzet.domain.user.entity.User;
import com.budzet.domain.user.service.UserService;
import com.budzet.global.api.ApiResponse;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import com.budzet.global.rq.Rq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Test
    @DisplayName("회원 가입 성공")
    void join_success() {

        UserService service = mock(UserService.class);
        Rq rq = mock(Rq.class);

        UserController controller =
                new UserController(service, rq);

        String email = "user1@test.com";
        String password = "password123";
        String name = "user1";

        User user = new User(
                email,
                "encodedPassword",
                name
        );

        when(service.join(email, password, name))
                .thenReturn(user);

        UserJoinRequest request =
                new UserJoinRequest(email, password, name);

        ResponseEntity<ApiResponse<UserDto>> response =
                controller.join(request);

        ApiResponse<UserDto> body = response.getBody();

        assertNotNull(body);
        assertEquals(201, response.getStatusCode().value());
        assertEquals(201, body.resultCode());
        assertEquals("회원가입이 완료되었습니다.", body.message());
        assertEquals(email, body.data().email());
        assertEquals(name, body.data().name());

        verify(service).join(email, password, name);
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {

        // given
        UserService service = mock(UserService.class);
        Rq rq = mock(Rq.class);

        UserController controller =
                new UserController(service, rq);

        String email = "user1@test.com";
        String password = "password123";
        String accessToken = "testAccessToken";
        String refreshToken = "testRefreshToken";

        User user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn(email);
        when(user.getName()).thenReturn("user1");

        UserLoginResponse loginResponse =
                UserLoginResponse.from(
                        user,
                        accessToken,
                        refreshToken
                );

        when(service.login(email, password))
                .thenReturn(loginResponse);

        UserLoginRequest request =
                new UserLoginRequest(email, password);

        // when
        ResponseEntity<ApiResponse<UserLoginResponse>> response =
                controller.login(request);

        ApiResponse<UserLoginResponse> body = response.getBody();

        // then
        assertNotNull(body);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(200, body.resultCode());
        assertEquals("로그인에 성공했습니다.", body.message());

        assertEquals(accessToken, body.data().accessToken());
        assertEquals(refreshToken, body.data().refreshToken());
        assertEquals(email, body.data().user().email());
        assertEquals("user1", body.data().user().name());

        verify(service).login(email, password);

        verify(rq)
                .addCookie("accessToken", accessToken);

        verify(rq)
                .addCookie("refreshToken", refreshToken);
    }

    @Test
    @DisplayName("Refresh Token으로 토큰 재발급 성공")
    void refresh_success() {

        // given
        UserService service = mock(UserService.class);
        Rq rq = mock(Rq.class);

        UserController controller =
                new UserController(service, rq);

        String refreshToken = "oldRefreshToken";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";

        // Authorization 헤더가 없으므로 쿠키 사용
        when(rq.getHeader("Authorization", ""))
                .thenReturn("");

        when(rq.getCookieValue("refreshToken", ""))
                .thenReturn(refreshToken);

        TokenRefreshResponse tokenResponse =
                TokenRefreshResponse.from(
                        newAccessToken,
                        newRefreshToken
                );

        when(service.refresh(refreshToken))
                .thenReturn(tokenResponse);

        // when
        ResponseEntity<ApiResponse<TokenRefreshResponse>> response =
                controller.refresh();

        ApiResponse<TokenRefreshResponse> body =
                response.getBody();

        // then
        assertNotNull(body);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(200, body.resultCode());
        assertEquals("토큰 재발급 성공", body.message());

        assertEquals(
                newAccessToken,
                body.data().accessToken()
        );

        assertEquals(
                newRefreshToken,
                body.data().refreshToken()
        );

        verify(service)
                .refresh(refreshToken);

        verify(rq)
                .addCookie("accessToken", newAccessToken);

        verify(rq)
                .addCookie("refreshToken", newRefreshToken);
    }

    @Test
    @DisplayName("Refresh Token이 없으면 인증 실패")
    void refresh_noRefreshToken() {

        // given
        UserService service = mock(UserService.class);
        Rq rq = mock(Rq.class);

        UserController controller =
                new UserController(service, rq);

        when(rq.getHeader("Authorization", ""))
                .thenReturn("");

        when(rq.getCookieValue("refreshToken", ""))
                .thenReturn("");

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                controller::refresh
        );

        // then
        assertEquals(
                ErrorCode.UNAUTHORIZED,
                exception.getErrorCode()
        );

        verify(service, never())
                .refresh(anyString());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() {
        // given
        UserService service = mock(UserService.class);
        Rq rq = mock(Rq.class);

        UserController controller =
                new UserController(service, rq);

        User user = mock(User.class);

        when(rq.getActor())
                .thenReturn(user);

        when(user.getId())
                .thenReturn(1L);

        // when
        ResponseEntity<ApiResponse<Void>> response =
                controller.logout();

        // then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(200, response.getBody().resultCode());
        assertEquals("로그아웃되었습니다.", response.getBody().message());
        assertNull(response.getBody().data());

        verify(rq).getActor();
        verify(service).logout(1L);

        verify(rq).deleteCookie("accessToken");
        verify(rq).deleteCookie("refreshToken");
    }
}