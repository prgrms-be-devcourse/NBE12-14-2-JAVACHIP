package com.budzet.domain.budget.entity;

import com.budzet.domain.budget.dto.BudgetUpdateRequest;
import com.budzet.domain.room.entity.Room;
import com.budzet.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class BudgetChange {

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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BudgetType type;

    @Column(length = 20)
    private String reason;

    private Long changeBudget;

    private Long changedBudget;

    @CreatedDate
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", unique = true)
    private BudgetRequest request;

    public BudgetChange(Room room, User user, BudgetUpdateRequest budgetUpdateRequest){
        this.room = room;
        this.user = user;
        this.userName = user.getName();
        this.type = budgetUpdateRequest.budgetType();
        this.reason = budgetUpdateRequest.changeReason();
        this.changeBudget = budgetUpdateRequest.totalBudget();
        this.changedBudget = budgetUpdateRequest.totalBudget();
    }
}
