package com.playbit.backend.room.dto;

import com.playbit.backend.room.Room;
import lombok.Getter;

@Getter
public class FinishedRoomDTO extends RoomDTO {

    private Long winnerMemberId;

    FinishedRoomDTO(Room room) {
        super(room.getStatus());
        this.winnerMemberId=room.getWinner().getMemberId();
    }

    public static FinishedRoomDTO from(Room room){
        return new FinishedRoomDTO(room);
    }
}
