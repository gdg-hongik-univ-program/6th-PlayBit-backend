package com.playbit.backend.notification;

import com.playbit.backend.member.Member;
import com.playbit.backend.room.Room;
import com.playbit.backend.webPush.WebPushRepository;
import com.playbit.backend.webPush.WebPushService;
import com.playbit.backend.webPush.WebPushSubscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.playbit.backend.webPush.WebPushSubscription.fromWebPushSubscription;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final WebPushRepository webPushRepository;
    private final WebPushService webPushService;
    private final NotificationRepository notificationRepository;



    // 미션 완료시 상대방에게 알림을 보냄
    public void missionCompleteNotification(Room room, Member opponent) {

        // 상대방의 구독 정보 가져오기
        List<WebPushSubscription> byMemberId = webPushRepository.findByMemberId(opponent.getMemberId());

        // 알림 생성하기
        Notification notification = Notification.builder()
                .member(opponent)
                .type(NotificationType.MISSION_COMPLETED)
                .title("상대방 턴 종료!")
                .content("당신의 차례입니다.")
                .createdAt(LocalDateTime.now())
                .isRead(false).build();
        notificationRepository.save(notification);

        // 서비스 워커로 보낼 메시지 Payload 작성 (Java 15+ Text Blocks 사용)
        String payload = String.format("""
                {
                    "title": "상대방 턴 종료!",
                    "body": "당신의 차례입니다.",
                    "url": "/rooms/%s"
                }
                """,room.getEntryCode());


        // 알림 발송 서비스 호출
        for (WebPushSubscription webPushSubscription : byMemberId) {
            webPushService.sendPush(fromWebPushSubscription(webPushSubscription), payload);
        }
    }
}
