package com.playbit.backend.notification;

import com.playbit.backend.member.Member;
import com.playbit.backend.webpush.WebPushService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final WebPushService webPushService;
    private final NotificationRepository notificationRepository;

    // 미션 완료시 상대방에게 알림을 보냄
    @Transactional
    public void missionCompleteNotification(String roomCode, List<Member> members) {
        String title = "상대방 턴 종료!";
        String content = "당신의 차례입니다. 어서 미션을 완료해 보세요!";

        createNotifications(members, NotificationType.MISSION_COMPLETED, title, content);
        sendNotifications(members, title, content, roomCode);
    }

    // 게임 시작시 플레이어들에게 알림을 보냄
    @Transactional
    public void roomStartedNotification(String roomCode, List<Member> members) {
        String title = "게임 시작!";
        String content = "모든 플레이어가 입장하여 게임이 시작됐어요! 누구의 턴인지 확인해보세요!";

        createNotifications(members, NotificationType.GAME_STARTED, title, content);
        sendNotifications(members, title, content, roomCode);
    }

    // 게임 종료시 플레이어들에게 알림을 보냄
    @Transactional
    public void roomFinishedNotification(String roomCode, List<Member> members) {
        String title = "게임 종료!";
        String content = "게임이 끝났어요! 결과를 확인해보세요!";

        createNotifications(members, NotificationType.GAME_FINISHED, title, content);
        sendNotifications(members, title, content, roomCode);
    }

    // 사보타주 완료시 상대방에게 알림을 보냄
    @Transactional
    public void sabotageCompleteNotification(String roomCode, List<Member> members) {
        String title = "사보타주!!";
        String content = "상대방의 사보타주로 제한시간이 6시간 감소했어요!";

        createNotifications(members, NotificationType.MISSION_SABOTAGED, title, content);
        sendNotifications(members, title, content, roomCode);
    }

    @Transactional
    public void createNotifications(List<Member> members, NotificationType type, String title, String content) {
        List<Notification> notifications = members.stream()
                .map(member -> Notification.builder()
                        .member(member)
                        .type(type)
                        .title(title)
                        .content(content)
                        .createdAt(LocalDateTime.now())
                        .isRead(false)
                        .build())
                .toList();
        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void sendNotifications(List<Member> members, String title, String content, String roomCode) {
        // 서비스 워커로 보낼 메시지 Payload 작성 (Java 15+ Text Blocks 사용)
        String payload = String.format(
                """
                                {
                                    "title": "%s",
                                    "body": "%s",
                                    "url": "/rooms/%s"
                                }
                                """,
                title, content, roomCode);

        // 알림 발송 서비스 호출
        webPushService.sendPushToMembers(members, payload);
    }
}
