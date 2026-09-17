package com.budzet.global.rq;

import com.budzet.domain.user.entity.User;
import com.budzet.domain.user.service.UserService;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {
    private final UserService userService;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public User getActor(){
        String headerAuthorization = getHeader("Authorization", "");

        String accessToken = null;

        if(!headerAuthorization.isBlank()){

            if(!headerAuthorization.startsWith("Bearer ")){
                throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
            }

            accessToken = headerAuthorization.substring(7);

        } else{
            accessToken = getCookieValue("accessToken", "");
        }


        if (accessToken.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Map<String, Object> payload =
                userService.payloadOrNull(accessToken);

        if (payload == null) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        Long id = ((Number) payload.get("id")).longValue();

        return userService.findById(id)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
                );
    }

    public String getCookieValue(String name, String defaultValue){
        return Optional
                .ofNullable(request.getCookies())
                .flatMap(
                        cookies ->
                                Arrays.stream(cookies)
                                        .filter(cookie -> cookie.getName().equals(name))
                                        .map(Cookie::getValue)
                                        .filter(value -> !value.isBlank())
                                        .findFirst()
                )
                .orElse(defaultValue);
    }

    public void addCookie(String name, String value){
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
    }

    public String getHeader(String name, String defaultValue){
        return Optional
                .ofNullable(request.getHeader(name))
                .filter(headerValue -> !headerValue.isBlank())
                .orElse(defaultValue);
    }
}
