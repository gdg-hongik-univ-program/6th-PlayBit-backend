package com.playbit.backend.room;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.playbit.backend.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Room {

    public Room(RoomStatus status, String category, String entryCode){
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

    @JsonIgnore //테스트 할때 지연로딩, 무한 루프 방지하기(공통 응답 DTO 추가시 삭제)
    @ManyToOne
    @JoinColumn(name = "winner_member_id")
    private Member winner;

    private String category;

    private Long currentTurnMemberId;

    private LocalDateTime turnStartedAt;

    private LocalDateTime turnDeadline;

    public void startGame(Long firstTurnMemberId ){
        this.status = RoomStatus.PLAYING;
        this.currentTurnMemberId = firstTurnMemberId;
        this.turnStartedAt = LocalDateTime.now();
        this.turnDeadline = LocalDateTime.now().plusHours(24);
    }

    public void updateCategory(String category){
        this.category = category;
    }
}
