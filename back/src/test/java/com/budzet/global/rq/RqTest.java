package com.budzet.global.rq;

import com.budzet.domain.user.entity.User;
import com.budzet.domain.user.service.UserService;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RqTest {

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private Rq rq;


    @Test
    @DisplayName("헤더를 통한 사용자 인증 성공")
    void getActor_header_success() {

        String accessToken = "validAccessToken";

        User user = new User(
                "test@test.com",
                "encodedPassword",
                "홍길동"
        );

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + accessToken);

        when(userService.payloadOrNull(accessToken))
                .thenReturn(Map.of(
                        "id", 1L,
                        "name", "홍길동"
                ));

        when(userService.findById(1L))
                .thenReturn(Optional.of(user));

        User result = rq.getActor();

        assertSame(user, result);
    }


    @Test
    @DisplayName("쿠키를 통한 사용자 인증 성공")
    void getActor_cookie_success() {

        String accessToken = "validAccessToken";

        User user = new User(
                "test@test.com",
                "encodedPassword",
                "홍길동"
        );

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        when(request.getCookies())
                .thenReturn(new Cookie[]{
                        new Cookie("accessToken", accessToken)
                });

        when(userService.payloadOrNull(accessToken))
                .thenReturn(Map.of(
                        "id", 1L,
                        "name", "홍길동"
                ));

        when(userService.findById(1L))
                .thenReturn(Optional.of(user));

        User result = rq.getActor();

        assertSame(user, result);
    }


    @Test
    @DisplayName("인증 실패- Access Token이 없을 때")
    void getActor_unauthorized() {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        when(request.getCookies())
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rq.getActor()
        );

        assertEquals(
                ErrorCode.UNAUTHORIZED,
                exception.getErrorCode()
        );
    }


    @Test
    @DisplayName("인증 실패- 유효하지 않은 Access Token")
    void getActor_invalidAccessToken() {

        String accessToken = "invalidAccessToken";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + accessToken);

        when(userService.payloadOrNull(accessToken))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rq.getActor()
        );

        assertEquals(
                ErrorCode.INVALID_ACCESS_TOKEN,
                exception.getErrorCode()
        );
    }
}