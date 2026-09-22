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

    public void updateTotalBudget(Long changedBudget, BudgetType type) {
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
    }

    public void settleBudget(Long requestedAmount, Long changedBudget){

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
        }

        public void updateTotalBudget(Long updateChanged){

            if(updateChanged == null){
                throw new BusinessException(ErrorCode.BAD_REQUEST,"입력된 금액이 올바르지 않습니다.");
            }

            //가용금액 - 차액 < 0 일때 예외
            /* 예)   실제예산  = 1000,     가용예산  = 400,
                     수정전 500, 수정후 1000월 일때 실제 금액과 가용예산에서 차액인 500원을 차감
                     가용예산 400 + (-500) = -100이기 때문에 예외처리
                     실제예산은 비교하지 않는 이유 실제예산이 가용예산보타 적은 상황은 없음*/
            if(this.availableBudget + updateChanged < 0){
                throw new BusinessException(ErrorCode.BUDGET_EXCEEDED,"가용예산을 초과하여 정산할 수 없습니다..");
            }

            //실제, 가용 금액 + 차액
            /* 예) 실제예산 = 1000, 가용예산 = 1000원일때
            *      수정하려는 정산건은 이미 가용예산과 실제 예산에 반영된 상태
            *         - 가용예산은 승인처리시
            *         - 실제예산은 정산처리시
            *
            *       그렇기에 차액을 두 예산에 똑같은 더하기 처리 */
            this.totalBudget = this.totalBudget + updateChanged;
            this.availableBudget = this.availableBudget + updateChanged;
        }
    }
