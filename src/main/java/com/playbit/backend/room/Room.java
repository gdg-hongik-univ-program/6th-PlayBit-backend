package com.playbit.backend.room;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.playbit.backend.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    public Room(RoomStatus status, Category category, String entryCode){
        this.status = status;
        this.category = category;
        this.entryCode = entryCode;
        this.currentTurnNumber = 1L;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    private String entryCode;

    @JsonIgnore //테스트 할때 지연로딩, 무한 루프 방지하기(공통 응답 DTO 추가시 삭제)
    @ManyToOne
    @JoinColumn(name = "winner_member_id")
    private Member winner;

    @Enumerated(EnumType.STRING)
    private Category category;

    private Long currentTurnMemberId;

    private Long currentTurnNumber;

    private LocalDateTime turnStartedAt;

    private LocalDateTime turnDeadline;

    private Boolean currentTurnSabotaged;

    public void startGame(Long firstTurnMemberId ){
        this.status = RoomStatus.PLAYING;
        this.currentTurnMemberId = firstTurnMemberId;
        this.turnStartedAt = LocalDateTime.now();
        this.turnDeadline = LocalDateTime.now().plusHours(24);
    }

    public void updateCategory(Category category){
        this.category = category;
    }



    public void turnFinished(Long nextTurnMemberId) {
        // 상대의 턴으로 넘기고
        this.setCurrentTurnMemberId(nextTurnMemberId);
        this.currentTurnNumber++;

        // 해당 시간을 기록하고
        LocalDateTime now = LocalDateTime.now();
        this.setTurnStartedAt(now);
        this.setTurnDeadline(now.plusHours(24));

        // 사보타주 변수를 초기화한다.
        this.setCurrentTurnSabotaged(false);
    }

    public void gameFinished(Member member) {
        this.setStatus(RoomStatus.FINISHED);
        this.setWinner(member);
        /*this.setCurrentTurnMemberId(null);
        this.setTurnStartedAt(null);
        this.setTurnDeadline(null);
        this.setCurrentTurnSabotaged(false);*/
    }
}
