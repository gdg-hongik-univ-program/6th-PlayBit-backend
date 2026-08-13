package com.playbit.backend.sse;

import com.playbit.backend.auth.LoginMember;
import com.playbit.backend.member.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "SSE API", description = "SSE 관련 API입니다.")
public class SseController {

    private final SseService sseService;

    @Operation(summary = "SSE 구독 요청", description = "SSE 구독을 요청합니다.")
    @GetMapping(value = "/{entryCode}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @PathVariable String entryCode,
            @LoginMember Member loginMember
    ) {
        // 세션에서 인증받은 memberId로 SSE Emitter 구독 반환
        return sseService.subscribe(entryCode, loginMember.getMemberId());
    }
}