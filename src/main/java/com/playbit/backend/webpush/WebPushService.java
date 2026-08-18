package com.playbit.backend.webpush;

import com.playbit.backend.member.Member;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.security.Security;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

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

    private final WebPushRepository webPushRepository;
    private PushService pushService;

    @PostConstruct
    public void init() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        pushService = new PushService(publicKey, privateKey, subject);
    }

    public URI createSubscription(Subscription subscription, Member member) {
        String uriStr = "api/subscriptions/" + member.getMemberId().toString();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uriStr);

        WebPushSubscription webPushSubscription = WebPushSubscription.builder()
                .member(member)
                .endpoint(subscription.endpoint)
                .p256dh(subscription.keys.p256dh)
                .auth(subscription.keys.auth)
                .build();

        webPushRepository.save(webPushSubscription);

        return builder.build().toUri();
    }

    public void sendPushToMembers(List<Member> members, String payloadJSON) {

        List<Long> memberIds = members.stream()
                .map(Member::getMemberId)
                .toList();

        List<WebPushSubscription> byMemberId2 = webPushRepository.findAllByMemberMemberIdIn((memberIds));

        for (WebPushSubscription webPushSubscription : byMemberId2) {
            try {
                Notification notification = new Notification(
                        webPushSubscription.getEndpoint(),
                        webPushSubscription.getP256dh(),
                        webPushSubscription.getAuth(),
                        payloadJSON
                );

                HttpResponse response = pushService.send(notification);
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 201) {
                    log.info("푸시 알림 전송 성공!");
                } else if (statusCode == 404 || statusCode == 410) {
                    log.warn("유효하지 않은 구독입니다. memberId: {}, endpoint: {}",
                            webPushSubscription.getMember().getMemberId(),
                            webPushSubscription.getEndpoint());
                } else {
                    log.warn("알림 전송 실패. 상태 코드: " + statusCode);
                }
            } catch (Exception e) {
                log.warn("푸시 발송 중 예외 발생: " + e.getMessage());
            }
        }
    }
}