package com.budzet.domain.user.controller;

import com.budzet.domain.user.dto.UserDto;
import com.budzet.domain.user.dto.UserJoinRequest;
import com.budzet.domain.user.dto.UserLoginRequest;
import com.budzet.domain.user.dto.UserLoginResponse;
import com.budzet.domain.user.entity.User;
import com.budzet.domain.user.service.UserService;
import com.budzet.global.api.ApiResponse;
import com.budzet.global.rq.Rq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Test
    @DisplayName("회원 가입 성공")
    void join_success() {

        UserService service =
                mock(UserService.class);

        Rq rq =
                mock(Rq.class);

        UserController controller =
                new UserController(service,  rq);

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

        ApiResponse<UserDto> response =
                controller.join(request);

        assertEquals(201, response.resultCode());
        assertEquals("회원가입이 완료되었습니다.", response.message());
        assertEquals(email, response.data().email());
        assertEquals(name, response.data().name());
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {

        UserService service =
                mock(UserService.class);

        Rq rq =
                mock(Rq.class);

        UserController controller =
                new UserController(service, rq);

        String email = "user1@test.com";
        String password = "password123";
        String accessToken = "testAccessToken";

        User user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn(email);
        when(user.getName()).thenReturn("user1");
        when(user.getPassword()).thenReturn("encodedPassword");

        when(service.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(service.genAccessToken(user))
                .thenReturn(accessToken);

        UserLoginRequest request =
                new UserLoginRequest(email, password);

        ApiResponse<UserLoginResponse> response =
                controller.login(request);

        assertEquals(200, response.resultCode());
        assertEquals("로그인에 성공했습니다.", response.message());

        assertEquals(accessToken, response.data().accessToken());
        assertEquals(email, response.data().user().email());
        assertEquals("user1", response.data().user().name());

        verify(service)
                .checkPassword(password, "encodedPassword");

        verify(rq)
                .addCookie("accessToken", accessToken);
    }
}