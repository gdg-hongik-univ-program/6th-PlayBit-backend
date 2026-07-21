package com.playbit.backend.mission.dto;

import com.playbit.backend.mission.Mission;

import java.time.LocalDateTime;

public record MissionDTO (
        Long position,
        Long completedByMemberId,
        LocalDateTime CompletedAt
){
    public static MissionDTO from(Mission mission){
        return new MissionDTO(
                mission.getPosition(),
                mission.getCompletedBy().getMemberId(),
                mission.getCompletedAt()
        );
    }
}
