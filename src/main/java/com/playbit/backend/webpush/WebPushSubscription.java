package com.playbit.backend.webpush;

import com.playbit.backend.member.Member;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "web_push_subscription")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebPushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기존 프로젝트의 Member 엔티티와 다대일(N:1) 지연 로딩 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 브라우저 푸시 서버의 목적지 URL (길이가 길 수 있으므로 512 지정)
    @Column(nullable = false, length = 512, unique = true)
    private String endpoint;

    // 클라이언트의 공개키 (Diffie-Hellman)
    @Column(nullable = false)
    private String p256dh;

    // 인증 비밀키
    @Column(nullable = false)
    private String auth;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public WebPushSubscription(Member member, String endpoint, String p256dh, String auth) {
        this.member = member;
        this.endpoint = endpoint;
        this.p256dh = p256dh;
        this.auth = auth;
    }
}
