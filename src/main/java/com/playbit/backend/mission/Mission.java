package com.playbit.backend.mission;

import com.playbit.backend.member.Member;
import com.playbit.backend.room.Room;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long missionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id" )
    private Room room;

    private Long position;

    @Enumerated(EnumType.STRING)
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by_member_id")
    private Member completedBy;

    private LocalDateTime completeAT;

    private boolean isSabotaged;
}
