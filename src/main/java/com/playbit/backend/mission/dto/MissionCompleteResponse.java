package com.playbit.backend.mission.dto;

import com.playbit.backend.room.dto.RoomDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MissionCompleteResponse {

    private RoomDto room;
    private MissionDto mission;
}
