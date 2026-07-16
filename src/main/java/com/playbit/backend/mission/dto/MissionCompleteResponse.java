package com.playbit.backend.mission.dto;

import com.playbit.backend.room.dto.RoomDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MissionCompleteResponse {

    private RoomDTO room;
    private MissionDTO mission;
}
