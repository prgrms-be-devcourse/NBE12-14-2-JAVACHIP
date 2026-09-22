package com.budzet.domain.user.controller;

import com.budzet.domain.user.dto.UserDto;
import com.budzet.domain.user.dto.UserJoinRequest;
import com.budzet.domain.user.dto.UserLoginRequest;
import com.budzet.domain.user.dto.UserLoginResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
