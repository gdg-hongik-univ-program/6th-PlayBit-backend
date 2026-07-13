package com.playbit.backend.mission;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.playbit.backend.member.Member;
import com.playbit.backend.room.Room;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long missionId;

    @JsonIgnore //테스트 할때 지연로딩, 무한 루프 방지하기(공통 응답 DTO 추가시 삭제)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id" )
    private Room room;

    private Long position;

    @Enumerated(EnumType.STRING)
    private Content content;

    @JsonIgnore //테스트 할때 지연로딩, 무한 루프 방지하기(공통 응답 DTO 추가시 삭제)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by_member_id")
    private Member completedBy;

    private LocalDateTime completeAT;

    private Boolean sabotaged;

    public Mission(Room room, Long position, Content content){
        this.room = room;
        this.position = position;
        this.content = content ;
    };
}
