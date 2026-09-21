package com.budzet.global.rq;

import com.budzet.domain.user.entity.User;
import com.budzet.global.security.SecurityUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RqTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private Rq rq;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("SecurityContext에서 인증된 사용자를 조회한다")
    void getActor_success() {

        // given
        User user = new User(
                "test@test.com",
                "encodedPassword",
                "홍길동"
        );

        SecurityUser securityUser = new SecurityUser(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        securityUser,
                        null,
                        securityUser.getAuthorities()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        // when
        User result = rq.getActor();

        // then
        assertSame(user, result);
    }

    @Test
    @DisplayName("Authorization 헤더 값을 조회한다")
    void getHeader_success() {

        // given
        when(request.getHeader("Authorization"))
                .thenReturn("Bearer accessToken");

        // when
        String result = rq.getHeader("Authorization", "");

        // then
        assertEquals("Bearer accessToken", result);
    }

    @Test
    @DisplayName("헤더가 없으면 기본값을 반환한다")
    void getHeader_defaultValue() {

        // given
        when(request.getHeader("Authorization"))
                .thenReturn(null);

        // when
        String result = rq.getHeader("Authorization", "");

        // then
        assertEquals("", result);
    }

    @Test
    @DisplayName("쿠키 값을 조회한다")
    void getCookieValue_success() {

        // given
        when(request.getCookies())
                .thenReturn(new Cookie[]{
                        new Cookie("accessToken", "testAccessToken")
                });

        // when
        String result =
                rq.getCookieValue("accessToken", "");

        // then
        assertEquals("testAccessToken", result);
    }

    @Test
    @DisplayName("쿠키가 없으면 기본값을 반환한다")
    void getCookieValue_defaultValue() {

        // given
        when(request.getCookies())
                .thenReturn(null);

        // when
        String result =
                rq.getCookieValue("accessToken", "");

        // then
        assertEquals("", result);
    }
}