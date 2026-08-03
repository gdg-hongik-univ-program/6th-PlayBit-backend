package com.playbit.backend.sse;

import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;
    private final MemberRepository memberRepository;

    // SSE 구독 요청 API
    @GetMapping(value = "/{entryCode}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @PathVariable String entryCode,
            @RequestHeader(value = "X-Member-Id") String memberUuid
    ) {
        // 인터셉터를 통과했으므로 안전하게 memberId를 조회
        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 구독 및 Emitter 반환
        return sseService.subscribe(entryCode, member.getMemberId());
    }
}
