package com.playbit.backend.common.event;

import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.notification.NotificationService;
import com.playbit.backend.s3.S3UploadService;
import com.playbit.backend.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameEventListener {

    private final SseService sseService;
    private final NotificationService notificationService;
    private final S3UploadService s3UploadService;
    private final MemberRepository memberRepository;

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGameStartedEvent(GameStartedEvent event) {

        // 게임 시작 정보를 방 전체에 전송
        sseService.broadcastToRoom(event.entryCode(), Map.of("message", "GAME_STARTED"));

        // 게임 시작 알림을 플레이어들에게 전송
        List<Member> members = memberRepository.findAllByMemberIdIn(event.memberIds());
        notificationService.roomStartedNotification(event.entryCode(), members);
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGameEndedEvent(GameEndedEvent event) {

        // 방에 있는 사람들에게 게임 종료 알림 발송
        sseService.broadcastToRoom(event.roomCode(), Map.of("message", "GAME_ENDED"));

        // 게임 종료 알림 보내기
        List<Member> members = memberRepository.findAllByMemberIdIn(event.memberIds());
        notificationService.roomFinishedNotification(event.roomCode(), members);
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMissionCompletedEvent(MissionCompletedEvent event) {

        List<Member> members = memberRepository.findAllByMemberIdIn(event.memberIds());

        // 게임 안 끝나고 턴만 넘어갈 때 알림 발송
        notificationService.missionCompleteNotification(event.roomCode(), members);

        // 방에 있는 사람들에게 미션 완료 알림 발송
        sseService.broadcastToRoom(event.roomCode(), Map.of("message", "MISSION_COMPLETED"));
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMissionSabotagedEvent(MissionSabotagedEvent event) {

        // 💡 리턴하기 직전, 사보타주 발생 알림 발송
        sseService.broadcastToRoom(event.roomCode(), Map.of("message", "MISSION_SABOTAGED"));

        // 리턴 전 상대방에게 알림 전송
        List<Member> members = memberRepository.findAllByMemberIdIn(event.memberIds());
        notificationService.sabotageCompleteNotification(event.roomCode(), members);
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoomUpdatedEvent(RoomUpdatedEvent event) {

        sseService.broadcastToRoom(event.entryCode(), Map.of("message", "TURN_TIMEOUT_UPDATED"));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleImageSaveFailedEvent(ImageSaveFailedEvent event) {

        s3UploadService.deleteImage(event.imageUrl());
    }

    @Recover
    public void recoverSendNotification(Exception e) {
        log.error("3회 재시도에도 불구하고 SSE 혹은 푸시 알림 발송 최종 실패");
        log.error("원인: {}", e.getMessage());
    }
}
