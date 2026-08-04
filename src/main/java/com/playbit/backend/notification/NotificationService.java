package com.playbit.backend.notification;

import com.playbit.backend.member.Member;
import com.playbit.backend.player.Player;
import com.playbit.backend.player.PlayerRepository;
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
    private final PlayerRepository playerRepository;


    // 미션 완료시 상대방에게 알림을 보냄
    public void missionCompleteNotification(Room room, Member opponent) {

        // 상대방의 구독 정보 가져오기
        List<WebPushSubscription> byMemberId = webPushRepository.findByMemberMemberId(opponent.getMemberId());

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

    // 게임 시작시 플레이어들에게 알림을 보냄
    public void roomStartedNotification(Room room) {

        // 해당 방의 모든 멤버 찾기
        List<Member> players = playerRepository.findByRoom(room).stream()
                .map(Player::getMember)
                .toList();

        // 멤버들의 구독 정보 가져오기
        List<WebPushSubscription> subscriptions = players.stream()
                .map(Member::getMemberId)
                .flatMap(memberId -> webPushRepository.findByMemberMemberId(memberId).stream())
                .toList();

        // 멤버들마다 알림 생성하고 저장하기
        for (Member member : players) {
            Notification notification = Notification.builder()
                    .member(member)
                    .type(NotificationType.GAME_FINISHED)
                    .title("게임 시작!")
                    .content("모든 플레이어가 입장하여 게임이 시작됐어요! 누구의 턴인지 확인해보세요!")
                    .createdAt(LocalDateTime.now())
                    .isRead(false).build();
            notificationRepository.save(notification);
        }
        // 서비스 워커로 보낼 메시지 Payload 작성 (Java 15+ Text Blocks 사용)
        String payload = String.format("""
                {
                    "title": "게임 시작!!",
                    "body": "모든 플레이어가 입장하여 게임이 시작됐어요! 누구의 턴인지 확인해보세요!",
                    "url": "/rooms/%s"
                }
                """,room.getEntryCode());


        // 알림 발송 서비스 호출
        for (WebPushSubscription webPushSubscription : subscriptions) {
            webPushService.sendPush(fromWebPushSubscription(webPushSubscription), payload);
        }
    }

    // 게임 종료시 플레이어들에게 알림을 보냄
    public void roomFinishedNotification(Room room) {

        // 해당 방의 모든 멤버 찾기
         List<Member> players = playerRepository.findByRoom(room).stream()
                .map(Player::getMember)
                 .toList();

         // 멤버들의 구독 정보 가져오기
        List<WebPushSubscription> subscriptions = players.stream()
                .map(Member::getMemberId)
                .flatMap(memberId -> webPushRepository.findByMemberMemberId(memberId).stream())
                .toList();

        // 멤버들마다 알림 생성하고 저장하기
        for ( Member member : players ) {
            Notification notification = Notification.builder()
                    .member(member)
                    .type(NotificationType.GAME_FINISHED)
                    .title("게임 종료!")
                    .content("게임이 끝났어요! 결과를 확인해보세요!")
                    .createdAt(LocalDateTime.now())
                    .isRead(false).build();
            notificationRepository.save(notification);
        }


        // 서비스 워커로 보낼 메시지 Payload 작성 (Java 15+ Text Blocks 사용)
        String payload = String.format("""
                {
                    "title": "게임 종료!",
                    "body": "게임이 끝났어요! 결과를 확인해보세요!",
                    "url": "/rooms/%s"
                }
                """,room.getEntryCode());


        // 알림 발송 서비스 호출
        for (WebPushSubscription webPushSubscription : subscriptions) {
            webPushService.sendPush(fromWebPushSubscription(webPushSubscription), payload);
        }
    }

    // 사보타주 완료시 상대방에게 알림을 보냄
    public void sabotageCompleteNotification(Room room, Member opponent) {

        // 상대방의 구독 정보 가져오기
        List<WebPushSubscription> byMemberId = webPushRepository.findByMemberMemberId(opponent.getMemberId());

        // 알림 생성하기
        Notification notification = Notification.builder()
                .member(opponent)
                .type(NotificationType.MISSION_SABOTAGED)
                .title("사보타주!")
                .content("상대방의 사보타주로 제한시간이 6시간 감소했어요!")
                .createdAt(LocalDateTime.now())
                .isRead(false).build();
        notificationRepository.save(notification);

        // 서비스 워커로 보낼 메시지 Payload 작성 (Java 15+ Text Blocks 사용)
        String payload = String.format("""
                {
                    "title": "사보타주!",
                    "body": "상대방의 사보타주로 제한시간이 6시간 감소했어요!",
                    "url": "/rooms/%s"
                }
                """,room.getEntryCode());


        // 알림 발송 서비스 호출
        for (WebPushSubscription webPushSubscription : byMemberId) {
            webPushService.sendPush(fromWebPushSubscription(webPushSubscription), payload);
        }
    }
}
