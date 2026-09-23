package com.budzet.domain.user.service;

import com.budzet.domain.user.entity.User;
import com.budzet.standard.ut.Ut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthTokenService {

    @Value("${custom.jwt.access-secret}")
    private String accessSecret;

    @Value("${custom.jwt.access-expire-millis}")
    private long accessExpireMillis;

    @Value("${custom.jwt.refresh-secret}")
    private String refreshSecret;

    @Value("${custom.jwt.refresh-expire-millis}")
    private long refreshExpireMillis;

    String genAccessToken(User user){
        return Ut.jwt.toString(
                accessSecret,
                accessExpireMillis,
                Map.of("id", user.getId()));
    }

    String genRefreshToken(User user){
        return Ut.jwt.toString(
                refreshSecret,
                refreshExpireMillis,
                Map.of("id", user.getId()));
    }

    Map<String, Object> accessPayloadOrNull(String accessToken){
        return payloadOrNull(accessToken, accessSecret);
    }

    Map<String, Object> refreshPayloadOrNull(String refreshToken){
        return payloadOrNull(refreshToken, refreshSecret);
    }

    private Map<String, Object> payloadOrNull(String jwt, String secret){
        Map<String, Object> payload = Ut.jwt.payloadOrNull(jwt, secret);

        if(payload == null){
            return null;
        }

        Long id = ((Number) payload.get("id")).longValue();

        return Map.of("id", id);
    }
}
