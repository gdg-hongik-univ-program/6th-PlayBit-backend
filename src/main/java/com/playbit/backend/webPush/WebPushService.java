package com.playbit.backend.webPush;

import com.playbit.backend.common.ErrorCode;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebPushService {

    @Value("${webpush.vapid.public-key}")
    private String publicKey;

    @Value("${webpush.vapid.private-key}")
    private String privateKey;

    @Value("${webpush.vapid.subject}")
    private String subject;

    private final MemberRepository memberRepository;
    private final WebPushRepository webPushRepository;
    private PushService pushService;

    @PostConstruct
    public void init() throws Exception {
        pushService = new PushService(publicKey, privateKey, subject);
    }

    public URI createSubscription(Subscription subscription, String memberUuid) {

        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        String uriStr = "api/subscriptions/" + member.getMemberId().toString();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uriStr);

        WebPushSubscription webPushSubscription
                = WebPushSubscription.builder()
                .member(member)
                .endpoint(subscription.endpoint)
                .p256dh(subscription.keys.p256dh)
                .auth(subscription.keys.auth).build();

        webPushRepository.save(webPushSubscription);

        return builder.build().toUri();
    }

    public void sendPush(Subscription subscription, String payloadJSON) {
        try {

            Notification notification = new Notification(
                    subscription.endpoint,
                    subscription.keys.p256dh,
                    subscription.keys.auth,
                    payloadJSON
            );

            HttpResponse response = pushService.send(notification);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 201) {
                log.info("푸시 알림 전송 성공!");
            } else if (statusCode == 404 || statusCode == 410) {
                // 3. 에러 처리: 사용자가 알림 권한을 철회했거나 구독이 만료된 경우
                log.warn("유효하지 않은 구독입니다. DB에서 해당 구독 정보를 삭제해야 합니다.");
                // TODO: DB에서 해당 subscription 삭제 로직 호출
            } else {
                log.warn("알림 전송 실패. 상태 코드: " + statusCode);
            } } catch (Exception e) {
            log.warn("푸시 발송 중 예외 발생: " + e.getMessage());
        }
    }
}
