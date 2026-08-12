package com.playbit.backend.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 미션 통계 응답")
public record MemberStatsResponse(
    @Schema(description = "닉네임", example = "player_one") String nickname,
    @Schema(description = "총 미션 달성 수", example = "42") int totalMissionSuccess,
    @Schema(description = "연속 달성 스트릭 (일)", example = "5") int consecutiveMissionStreak
) {}
