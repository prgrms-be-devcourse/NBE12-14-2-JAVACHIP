package com.budzet.domain.room.entity;

import com.budzet.domain.budget.entity.BudgetChange;
import com.budzet.domain.budget.entity.BudgetRequest;
import com.budzet.domain.invite.entity.Invite;
import com.budzet.domain.budget.entity.BudgetType;
import com.budzet.global.exception.BusinessException;
import com.budzet.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name;

    private Long totalBudget;

    private Long availableBudget;

    @Enumerated(EnumType.STRING)
    @Column(length = 3, nullable = false)
    private Currency currency;

    @CreatedDate
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE)
    private List<UserRoomConnection> userConnections = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE)
    private List<BudgetRequest> budgetRequests = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE)
    private List<BudgetChange> budgetChanges = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE)
    private List<Invite> invites = new ArrayList<>();

    private Room(String name, Long totalBudget, Currency currency) {
        this.name = name;
        this.totalBudget = totalBudget;
        this.availableBudget = totalBudget;
        this.currency = currency;
    }

    public static Room create(String name, Long totalBudget, Currency currency) {
        return new Room(name, totalBudget, currency);
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void addBudgetChange(BudgetChange budgetChange){
        this.budgetChanges.add(budgetChange);
    }

    public Room updateTotalBudget(Long changedBudget, BudgetType type) {
        if(changedBudget == null || changedBudget <= 0 ){
            throw new BusinessException(ErrorCode.BAD_REQUEST,"입력된 금액이 올바르지 않습니다.");
        }
        if (type == BudgetType.INCREASE) {
            this.totalBudget = this.totalBudget + changedBudget;
            this.availableBudget = this.availableBudget + changedBudget;

        }else if(type == BudgetType.DECREASE){
            if(availableBudget < changedBudget){
                throw new BusinessException(ErrorCode.BUDGET_EXCEEDED);
            }
            this.totalBudget = this.totalBudget - changedBudget;
            this.availableBudget = this.availableBudget - changedBudget;

        }
        return this;
    }

    public Room settleBudget(Long requestedAmount, Long changedBudget){

            if(changedBudget == null || changedBudget <= 0 ){
                throw new BusinessException(ErrorCode.BAD_REQUEST,"입력된 금액이 올바르지 않습니다.");
            }

            if(this.totalBudget < changedBudget){
                throw new BusinessException(ErrorCode.BUDGET_EXCEEDED,"실제예산을 초과하여 정산할 수 없습니다..");
            }

            // 승인 금액 이상으로 정산 처리할 경우 예외처리
            if(changedBudget > requestedAmount) {
                throw new BusinessException(ErrorCode.SETTLEMENT_AMOUNT_EXCEEDS_APPROVED);
            }

            //정산금액 처리
            Long balanceBudget = requestedAmount - changedBudget; //잔액 = 신청 승인된 금액 - 실제 사용 금액
            this.totalBudget = this.totalBudget - changedBudget;  //실제 예산 - 정산 금액
            this.availableBudget = this.availableBudget + balanceBudget;  //가용 예산 + 잔액

            return this;
        }
    }
