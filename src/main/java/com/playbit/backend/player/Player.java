package com.playbit.backend.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.playbit.backend.member.Member;
import com.playbit.backend.room.Room;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Player {

    public Player(Room room, Member member, PlayerRole role){
        this.room = room;
        this.member = member;
        this.role = role;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playerId;

    @JsonIgnore //테스트 할때 지연로딩, 무한 루프 방지하기(공통 응답 DTO 추가시 삭제)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @JsonIgnore //테스트 할때 지연로딩, 무한 루프 방지하기(공통 응답 DTO 추가시 삭제)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private PlayerRole role;

}
