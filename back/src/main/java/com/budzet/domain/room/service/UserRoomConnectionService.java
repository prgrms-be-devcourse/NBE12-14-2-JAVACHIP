package com.budzet.domain.room.service;

import com.budzet.domain.room.entity.Authority;
import com.budzet.domain.room.entity.UserRoomConnection;
import com.budzet.domain.room.repository.UserRoomConnectionRepository;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.budzet.domain.room.dto.MemberResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoomConnectionService {

    private final UserRoomConnectionRepository userRoomConnectionRepository;

    public UserRoomConnection getConnection(Long roomId, Long userId) {

        return userRoomConnectionRepository
                .findByUser_IdAndRoom_Id(userId, roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getMembers(Long roomId) {
        return userRoomConnectionRepository.findAllByRoom_Id(roomId)
                .stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional
    public void kickMember(Long roomId, Long userId) {

        UserRoomConnection connection =
                userRoomConnectionRepository
                        .findByUser_IdAndRoom_Id(userId, roomId)
                        .orElseThrow(() ->
                                new BusinessException(ErrorCode.MEMBER_NOT_FOUND)
                        );

        userRoomConnectionRepository.delete(connection);
    }

    @Transactional
    public void leaveRoom(Long roomId, Long userId) {

        UserRoomConnection connection =
                userRoomConnectionRepository
                        .findByUser_IdAndRoom_Id(userId, roomId)
                        .orElseThrow(() ->
                                new BusinessException(ErrorCode.MEMBER_NOT_FOUND)
                        );

        if(connection.getAuthority() == Authority.OWNER) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }

        userRoomConnectionRepository.delete(connection);
    }
}