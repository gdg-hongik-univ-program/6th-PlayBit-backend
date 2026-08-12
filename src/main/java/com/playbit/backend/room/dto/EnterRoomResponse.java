package com.playbit.backend.room.dto;

import com.playbit.backend.mission.Mission;
import com.playbit.backend.player.Player;
import com.playbit.backend.player.PlayerRole;
import com.playbit.backend.room.Category;
import com.playbit.backend.room.RoomStatus;
import java.time.LocalDateTime;
import java.util.List;

public record EnterRoomResponse(
        String entryCode,
        RoomStatus status,
        Category category,
        Long myMemberId, // 내 멤버 ID 관전자면 null
        Long currentTurnMemberId, // FINISHED 상태일때는 null
        LocalDateTime turnStartedAt, // FINISHED 상태일때는 null
        LocalDateTime turnDeadline, // FINISHED 상태일때는 null
        Boolean currentTurnSabotaged, // FINISHED 상태일때는 null
        List<MissionItem> missions,
        List<PlayerItem> players,
        Long winnerMemberId // PLAYING 상태일때는 null
        ) {
    public record MissionItem(Long position, String content, Long completedByMemberId, LocalDateTime completedAt) {
        public static MissionItem from(Mission mission) {
            // completedBy가 null인지 먼저 확인하고, null이 아니면 memberId를 추출
            Long completedMemberId = (mission.getCompletedBy() != null)
                    ? mission.getCompletedBy().getMemberId()
                    : null;

            return new MissionItem(
                    mission.getPosition(),
                    mission.getContent().getDescription(),
                    completedMemberId,
                    mission.getCompletedAt());
        }
    }

    public record PlayerItem(Long memberId, PlayerRole role) {
        public static PlayerItem from(Player player) {
            return new PlayerItem(player.getMember().getMemberId(), player.getRole());
        }
    }
}
