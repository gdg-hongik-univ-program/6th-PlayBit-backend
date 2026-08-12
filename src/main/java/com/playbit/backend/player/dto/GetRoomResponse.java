package com.playbit.backend.player.dto;

import com.playbit.backend.room.Room;

import java.util.List;

public record GetRoomResponse(
        List<RoomInfo> roomInfos
) {
    public record RoomInfo(
            String roomName,
            String roomStatus,
            String entryCode
    ) {
        public static RoomInfo fromRoom(Room room) {
            return new RoomInfo(
                    room.getRoomName(),
                    room.getStatus().name(),
                    room.getEntryCode()
            );
        }
    }
}
