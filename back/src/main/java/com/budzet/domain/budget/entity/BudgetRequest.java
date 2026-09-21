package com.budzet.domain.budget.entity;

import com.budzet.domain.room.entity.Room;
import com.budzet.domain.user.entity.User;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class BudgetRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 50)
    private String userName;

    @Column(length = 20)
    private String reason;

    private Long requestedAmount;

    @Column(length = 20)
    private String status;

    @Column(length = 50)
    private String rejectReason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public BudgetRequest(Room room, User user, String reason, Long requestedAmount){
        this.room = room;
        this.user = user;
        this.userName = user.getName();
        this.reason = reason;
        this.requestedAmount = requestedAmount;
    }
    //todo 신청 상태 enum으로 변경 시 수정
    public void changeToSettlement(String status){
        if(!"APPROVED".equals(this.status)){
            throw new BusinessException(ErrorCode.BUDGET_REQUEST_NOT_APPROVED);
        }

        this.status = status;
    }
}
