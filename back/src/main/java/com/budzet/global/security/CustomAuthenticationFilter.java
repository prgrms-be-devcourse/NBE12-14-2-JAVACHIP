package com.budzet.global.security;

import com.budzet.domain.user.entity.User;
import com.budzet.domain.user.service.UserService;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import com.budzet.global.rq.Rq;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final Rq rq;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        logger.debug("CustomAuthenticationFilter called");

        String headerAuthorization = rq.getHeader("Authorization", "");

        String accessToken = null;

        if(!headerAuthorization.isBlank()){

            if(!headerAuthorization.startsWith("Bearer ")){
                throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
            }

            accessToken = headerAuthorization.substring(7);

        } else{
            accessToken = rq.getCookieValue("accessToken", "");
        }


        if (accessToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        Map<String, Object> payload =
                userService.payloadOrNull(accessToken);

        if (payload == null) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        Long id = ((Number) payload.get("id")).longValue();

        User user =  userService.findById(id)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        UserDetails securityUser = new SecurityUser(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                securityUser,
                null,
                securityUser.getAuthorities()
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

}
