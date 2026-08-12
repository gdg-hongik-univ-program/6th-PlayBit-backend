package com.playbit.backend.room.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.playbit.backend.room.Room;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PlayingRoomDto extends RoomDto {

    private final Long currentTurnMemberId;
    private final Long currentTurnNumber;
    private final LocalDateTime turnStartedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime turnDeadline;

    private final Boolean currentTurnSabotaged;

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
