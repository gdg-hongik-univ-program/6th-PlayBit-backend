package com.playbit.backend.room.dto;

import com.playbit.backend.room.Room;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
public class PlayingRoomDTO extends RoomDTO {

    private Long currentTurnMemberId;
    private Long currentTurnNumber;
    private LocalDateTime turnStartedAt;
    private LocalDateTime turnDeadline;
    private Boolean currentTurnSabotaged;

    PlayingRoomDTO(Room room) {
        super(room.getStatus());
        this.currentTurnMemberId = room.getCurrentTurnMemberId();
        this.currentTurnNumber = room.getCurrentTurnNumber();
        this.turnStartedAt = room.getTurnStartedAt();
        this.turnDeadline = room.getTurnDeadline();
        this.currentTurnSabotaged = room.getCurrentTurnSabotaged();
    }

    public static PlayingRoomDTO from(Room room){
        return new PlayingRoomDTO(room);
    }
}

