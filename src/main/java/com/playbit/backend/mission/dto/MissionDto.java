package com.playbit.backend.mission.dto;

import com.playbit.backend.mission.Mission;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "미션 응답 Dto")
public record MissionDto(
        @Schema(description = "미션 위치", example = "1") Long position,
        @Schema(description = "완료한 회원 ID", example = "7") Long completedByMemberId,
        @Schema(description = "완료 시각", example = "2026-08-03T15:26:55.488320") LocalDateTime completedAt,
        @Schema(
                description = "미션 인증 사진 URL",
                example = "https://{버킷이름}.s3.ap-northeast-2.amazonaws.com/missions/123e4567_image.jpg")
        String imageUrl,
        @Schema(description = "사보타주 여부", example = "true") Boolean sabotagedByOpponent,
        @Schema(
                description = "사보타주 인증 사진 URL",
                example = "https://{버킷이름}.s3.ap-northeast-2.amazonaws.com/sabotage/987f6543_image.jpg")
        String sabotageImageUrl,
        @Schema(description = "미션 완료 코멘트", example = "백엔드 개념학습 완료") String comment,
        @Schema(description = "사보타주 코멘트", example = "백엔드 개념학습 완료") String sabotageComment) {
    public static MissionDto from(Mission mission) {
        return new MissionDto(
                mission.getPosition(),
                mission.getCompletedBy() != null ? mission.getCompletedBy().getMemberId() : null,
                mission.getCompletedAt(),
                mission.getImageUrl(),
                mission.getSabotagedByOpponent(),
                mission.getSabotageImageUrl(),
                mission.getComment(),
                mission.getSabotageComment());
    }
}