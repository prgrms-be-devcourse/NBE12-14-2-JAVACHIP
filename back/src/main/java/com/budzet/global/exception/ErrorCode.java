package com.budzet.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효한 Access Token이 아닙니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    CONFLICT(HttpStatus.CONFLICT, "요청이 현재 상태와 충돌합니다."),
    USER_NOT_JOINED_ROOM(HttpStatus.FORBIDDEN, "사용자가 방에 속해있지 않습니다."),
    REQUEST_AMOUNT_OVER_BUDGET(HttpStatus.BAD_REQUEST, "신청예산이 가용예산을 초과했습니다."),
    USER_CONFLICT(HttpStatus.CONFLICT, "이미 존재하는 회원입니다."),
    BUDGET_NOT_FOUND(HttpStatus.NOT_FOUND,"해당 모임에 등록된 예산 정보가 없습니다."),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND,"해당 모임이 존재하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
