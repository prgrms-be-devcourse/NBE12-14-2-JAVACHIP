package com.budzet.domain.user.service;

import com.budzet.domain.user.entity.User;
import com.budzet.standard.ut.Ut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthTokenService {

    @Value("${custom.jwt.secret-pattern}")
    private String secretPattern;

    @Value("${custom.jwt.expire-millis}")
    private long expireMillis;

    String genAccessToken(User user){
        return Ut.jwt.toString(
                secretPattern,
                expireMillis,
                Map.of("id", user.getId(), "name", user.getName()));
    }

    Map<String, Object> payloadOrNull(String jwt){
        Map<String, Object> payload = Ut.jwt.payloadOrNull(jwt, secretPattern);

        if(payload == null){
            return null;
        }

        Long id = ((Number) payload.get("id")).longValue();
        String name = (String)payload.get("name");

        return Map.of("id", id, "name", name);
    }
}
