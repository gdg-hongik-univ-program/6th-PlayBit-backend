package com.playbit.backend.mission.dto;

import com.playbit.backend.mission.Mission;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "미션 응답 DTO")
public record MissionDTO (
        @Schema(description = "미션 위치", example = "1") Long position,
        @Schema(description = "완료한 회원 ID", example = "7") Long completedByMemberId,
        @Schema(description = "완료 시각", example = "2026-08-03T15:26:55.488320") LocalDateTime completedAt,
        @Schema(description = "미션 인증 사진 URL", example = "https://{버킷이름}.s3.ap-northeast-2.amazonaws.com/missions/123e4567_image.jpg")
        String imageUrl
) {
    public static MissionDTO from(Mission mission){
        return new MissionDTO(
                mission.getPosition(),
                mission.getCompletedBy().getMemberId(),
                mission.getCompletedAt(),
                mission.getImageUrl()
        );
    }
}