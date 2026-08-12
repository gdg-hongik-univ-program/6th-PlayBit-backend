package com.playbit.backend.room.dto;

import com.playbit.backend.room.Room;
import lombok.Getter;

@Getter
public class FinishedRoomDto extends RoomDto {

    private final Long winnerMemberId;
    private final Boolean isDraw;

    FinishedRoomDto(Room room) {
        super(room.getStatus());
        this.winnerMemberId = (room.getWinner() != null) ? room.getWinner().getMemberId() : null;
        this.isDraw = (room.getIsDraw() != null) ? room.getIsDraw() : false;
    }

    public static FinishedRoomDto from(Room room) {
        return new FinishedRoomDto(room);
    }
}
