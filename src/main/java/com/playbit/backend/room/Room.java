package com.playbit.backend.room;

import com.playbit.backend.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    public Room(RoomStatus status, Category category, String entryCode) {
        this.status = status;
        this.category = category;
        this.entryCode = entryCode;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    private String entryCode;

    @ManyToOne
    @JoinColumn(name = "winner_member_id")
    private Member winner;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(unique = true)
    private String roomName;

    private Long currentTurnMemberId;

    private Long currentTurnNumber;

    private LocalDateTime turnStartedAt;

    private LocalDateTime turnDeadline;

    private Boolean currentTurnSabotaged;

    private Boolean isDraw;

    @Version
    private Long version;

    public void startGame(Long firstTurnMemberId) {
        this.status = RoomStatus.PLAYING;
        this.currentTurnMemberId = firstTurnMemberId;
        this.currentTurnNumber = 1L;
        this.turnStartedAt = LocalDateTime.now();
        this.turnDeadline = LocalDateTime.now().plusHours(24);
    }

    public void updateCategory(Category category) {
        this.category = category;
    }

    public void updateRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void missionSabotaged() {
        this.currentTurnSabotaged = true;
        this.turnDeadline = turnDeadline.minusHours(6L);
    }

    public void turnFinished(Long nextTurnMemberId) {
        // 상대의 턴으로 넘기고
        this.currentTurnMemberId = nextTurnMemberId;
        this.currentTurnNumber++;

        // 해당 시간을 기록하고
        LocalDateTime now = LocalDateTime.now();
        this.turnStartedAt = now;
        this.turnDeadline = now.plusHours(24);

        // 사보타주 변수를 초기화한다.
        this.currentTurnSabotaged = false;
    }

    public void gameFinished(Member member) {
        this.status = RoomStatus.FINISHED;
        this.winner = member;
        this.isDraw = false;
    }

    public void gameFinishedAsDraw() {
        this.status = RoomStatus.FINISHED;
        this.isDraw = true;
    }
}
