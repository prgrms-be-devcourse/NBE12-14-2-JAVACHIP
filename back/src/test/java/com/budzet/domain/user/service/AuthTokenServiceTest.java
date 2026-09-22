package com.budzet.domain.user.service;

import com.budzet.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthTokenServiceTest {

    private AuthTokenService authTokenService;

    @BeforeEach
    void setUp() {
        authTokenService = new AuthTokenService();

        ReflectionTestUtils.setField(
                authTokenService,
                "secretPattern",
                "test-secret-key-for-budzet-jwt-authentication-123456789"
        );

        ReflectionTestUtils.setField(
                authTokenService,
                "expireMillis",
                3600L
        );
    }

    @Test
    @DisplayName("Access Token 생성 및 Payload 조회 성공")
    void genAccessToken_success() {

        User user = new User(
                "user1@test.com",
                "encodedPassword",
                "user1"
        );

        ReflectionTestUtils.setField(user, "id", 1L);

        String accessToken =
                authTokenService.genAccessToken(user);

        assertNotNull(accessToken);

        Map<String, Object> payload =
                authTokenService.accessPayloadOrNull(accessToken);

        assertNotNull(payload);
        assertEquals(1L, payload.get("id"));
        assertEquals("user1", payload.get("name"));
    }

    @Test
    @DisplayName("유효하지 않은 Access Token은 Payload 조회 실패")
    void invalidAccessToken() {

        String invalidToken = "invalid.jwt.token";

        Map<String, Object> payload =
                authTokenService.accessPayloadOrNull(invalidToken);

        assertNull(payload);
    }
}
