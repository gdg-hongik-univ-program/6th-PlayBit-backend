package com.playbit.backend.player.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이어 입장 응답 DTO")
public record PlayerJoinResponse(
        @Schema(description = "플레이어 ID", example = "5") Long playerId,
        @Schema(description = "회원 ID", example = "34L") Long memberId,
        @Schema(description = "역할", example = "O, X") String role
) {
}
