package com.playbit.backend.player.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이어 입장 응답 DTO")
public record PlayerJoinResponse(
        @Schema(description = "플레이어 ID", example = "5") Long playerId,
        @Schema(description = "회원 UUID", example = "3cf20bbb-fa6a-4487-80a4-ac8ff0a97881") String memberUuid,
        @Schema(description = "방 ID", example = "12") Long roomId
) {
}
