package com.playbit.backend.mission;

import com.playbit.backend.member.Member;
import com.playbit.backend.room.Room;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long missionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    private Long position;

    @Enumerated(EnumType.STRING)
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by_member_id")
    private Member completedBy;

    private LocalDateTime completedAt;

    @Column(length = 500)
    private String imageUrl;

    private Boolean sabotagedByOpponent = false;

    @Column(length = 500)
    private String sabotageImageUrl;

    @Column(length = 255)
    private String comment;

    @Column(length = 255)
    private String sabotageComment;

    public Mission(Room room, Long position, Content content) {
        this.room = room;
        this.position = position;
        this.content = content;
    }

    public void completeMission(Member member, String imageUrl, String comment) {
        this.completedBy = member;
        this.completedAt = LocalDateTime.now();
        this.imageUrl = imageUrl;
        this.comment = comment;
    }

    public void sabotageMission(String sabotageImageUrl, String sabotageComment) {
        this.sabotagedByOpponent = true;
        this.sabotageImageUrl = sabotageImageUrl;
        this.sabotageComment = sabotageComment;
    }
}
