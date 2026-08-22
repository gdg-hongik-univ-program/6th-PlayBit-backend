package com.playbit.backend.room.dto;

import com.playbit.backend.room.Room;
import lombok.Getter;

@Getter
public class FinishedRoomDTO extends RoomDTO {

    private Long winnerMemberId;
    private Boolean isDraw;

    FinishedRoomDTO(Room room) {
        super(room.getStatus());

        // winner가 존재할 때만 MemberId를 가져오고, 없으면 null을 할당합니다.
        this.winnerMemberId = (room.getWinner() != null)
                ? room.getWinner().getMemberId()
                : null;

        this.isDraw = (room.getIsDraw() != null)
                ? room.getIsDraw()
                : false;
    }

    public static FinishedRoomDTO from(Room room){
        return new FinishedRoomDTO(room);
    }
}
