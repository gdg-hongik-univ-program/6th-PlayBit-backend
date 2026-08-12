package com.playbit.backend.mission.dto;

import com.playbit.backend.room.dto.RoomDto;

public record MissionCompleteResponse(RoomDto room, MissionDto mission) {}
