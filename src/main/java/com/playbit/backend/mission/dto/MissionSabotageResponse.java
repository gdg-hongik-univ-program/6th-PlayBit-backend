package com.playbit.backend.mission.dto;

import com.playbit.backend.room.dto.PlayingRoomDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사보타주 응답 DTO")
public record MissionSabotageResponse(
        @Schema(description = "진행 중인 방 정보") PlayingRoomDTO room,
        @Schema(description = "사보타주 처리된 미션 정보") MissionDTO mission
) {
}