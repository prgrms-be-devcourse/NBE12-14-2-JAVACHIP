package com.budzet.domain.user.controller;

import com.budzet.domain.user.dto.*;
import com.budzet.domain.user.entity.User;
import com.budzet.domain.user.service.UserService;
import com.budzet.global.api.ApiResponse;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import com.budzet.global.rq.Rq;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final Rq rq;


    @PostMapping("/join")
    public ResponseEntity<ApiResponse<UserDto>> join(
            @RequestBody @Valid UserJoinRequest reqBody
    ){
        User user = userService.join(reqBody.email(), reqBody.password(), reqBody.name());

        return ApiResponse.response(
                HttpStatus.CREATED,
                "회원가입이 완료되었습니다.",
                new UserDto(user)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> login(
            @RequestBody @Valid UserLoginRequest reqBody
    ){
        UserLoginResponse response = userService.login(reqBody.email(), reqBody.password());

        rq.addCookie("accessToken", response.accessToken());
        rq.addCookie("refreshToken", response.refreshToken());

        return ApiResponse.response(
                HttpStatus.OK,
                "로그인에 성공했습니다.",
                response
        );
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(){
        User actor = rq.getActor();

        userService.logout(actor.getId());

        rq.deleteCookie("accessToken");
        rq.deleteCookie("refreshToken");

        return ApiResponse.response(
                HttpStatus.OK,
                "로그아웃되었습니다.",
                null
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh( ){
        String headerAuthorization = rq.getHeader("Authorization", "");

        String refreshToken = null;

        if(!headerAuthorization.isBlank()){
            if(!headerAuthorization.startsWith("Bearer ")){
                throw new BusinessException(ErrorCode.INVALID_AUTHORIZATION_HEADER);
            }

            refreshToken = headerAuthorization.substring(7);
        }else {
            refreshToken = rq.getCookieValue("refreshToken", "");
        }

        if(refreshToken.isBlank()){
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        TokenRefreshResponse tokenResponse = userService.refresh(refreshToken);

        rq.addCookie("accessToken", tokenResponse.accessToken());
        rq.addCookie("refreshToken", tokenResponse.refreshToken());

        return ApiResponse.response(
                HttpStatus.OK,
                "토큰 재발급 성공",
                tokenResponse
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> me(){
        User actor = rq.getActor();

        return ApiResponse.response(
                HttpStatus.OK,
                "내 정보 조회에 성공했습니다.",
                new UserDto(actor)
        );
    }

}
