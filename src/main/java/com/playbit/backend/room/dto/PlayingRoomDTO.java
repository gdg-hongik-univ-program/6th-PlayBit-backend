package com.playbit.backend.room.dto;

import com.playbit.backend.room.Room;
import lombok.Getter;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
public class PlayingRoomDto extends RoomDto {

    private Long currentTurnMemberId;
    private Long currentTurnNumber;
    private LocalDateTime turnStartedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime turnDeadline;
    private Boolean currentTurnSabotaged;

    PlayingRoomDto(Room room) {
        super(room.getStatus());
        this.currentTurnMemberId = room.getCurrentTurnMemberId();
        this.currentTurnNumber = room.getCurrentTurnNumber();
        this.turnStartedAt = room.getTurnStartedAt();
        this.turnDeadline = room.getTurnDeadline();
        this.currentTurnSabotaged = room.getCurrentTurnSabotaged();
    }

    public static PlayingRoomDto from(Room room) {
        return new PlayingRoomDto(room);
    }
}
