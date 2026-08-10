package com.playbit.backend.common.event;

import com.playbit.backend.notification.NotificationService;
import com.playbit.backend.sse.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GameEventListener {

    private final SseService sseService;
    private final NotificationService notificationService;

    @EventListener
    public void handleGameStartedEvent(GameStartedEvent event) {

        // 게임 시작 정보를 방 전체에 전송
        sseService.broadcastToRoom(event.entryCode(), Map.of("message", "GAME_STARTED"));

        // 게임 시작 알림을 플레이어들에게 전송
        notificationService.roomStartedNotification(event.entryCode(), event.players());
    }

    @EventListener
    public void handleGameEndedEvent(GameEndedEvent event) {

        // 방에 있는 사람들에게 게임 종료 알림 발송
        sseService.broadcastToRoom(event.roomCode(), Map.of("message", "GAME_ENDED"));

        // 게임 종료 알림 보내기
        notificationService.roomFinishedNotification(event.roomCode(), event.roomMembers());
    }

    @EventListener
    public void handleMissionCompletedEvent(MissionCompletedEvent event) {

        // 게임 안 끝나고 턴만 넘어갈 때 알림 발송
        notificationService.missionCompleteNotification(event.roomCode(), event.members());

        // 방에 있는 사람들에게 미션 완료 알림 발송
        sseService.broadcastToRoom(event.roomCode(), Map.of("message", "MISSION_COMPLETED"));
    }

    @EventListener
    public void handleMissionSabotagedEvent(MissionSabotagedEvent event) {

        // 💡 리턴하기 직전, 사보타주 발생 알림 발송
        sseService.broadcastToRoom(event.roomCode(), Map.of("message", "MISSION_SABOTAGED"));

        // 리턴 전 상대방에게 알림 전송
        notificationService.sabotageCompleteNotification(event.roomCode(), event.members());
    }

    @EventListener
    public void handleRoomUpdatedEvent(RoomUpdatedEvent event) {

        sseService.broadcastToRoom(event.entryCode(), Map.of("message", "TURN_TIMEOUT_UPDATED"));

    }
}
