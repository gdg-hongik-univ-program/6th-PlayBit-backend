package com.playbit.backend.room;

import com.playbit.backend.member.Member;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    private String entryCode;

    @ManyToOne
    @JoinColumn(name = "winner_member_id")
    private Member winner;

    private String category;

    private Long currentTurnMemberId;

    private LocalDateTime turnStartedAt;

    private LocalDateTime turnDeadline;
}
