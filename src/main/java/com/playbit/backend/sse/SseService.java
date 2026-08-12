package com.playbit.backend.sse;

import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class SseService {

    private final SseRepository sseRepository;
    private static final Long TIMEOUT = 60L * 1000 * 60; // 1시간

    // 클라이언트가 구독(sub)을 요청할 때 실행되는 메서드
    public SseEmitter subscribe(String entryCode, Long memberId) {
        String emitterId = entryCode + "_" + memberId;
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        sseRepository.save(emitterId, emitter);

        // 네트워크가 끊기거나 1시간이 지나면 자동으로 저장소에서 삭제되도록 콜백 등록 (메모리 누수 방지)
        emitter.onCompletion(() -> sseRepository.deleteById(emitterId));
        emitter.onTimeout(() -> sseRepository.deleteById(emitterId));
        emitter.onError((e) -> sseRepository.deleteById(emitterId));

        // 503 에러 방지: 연결 직후 반드시 더미 데이터를 한 번 보내야 함
        sendToClient(emitter, emitterId, "Connected successfully. [memberId=" + memberId + "]");

        return emitter;
    }

    // 특정 1명의 파이프에 데이터를 쏘는 헬퍼 메서드
    private void sendToClient(SseEmitter emitter, String emitterId, Object data) {
        try {
            emitter.send(
                    SseEmitter.event()
                            .id(emitterId)
                            .name("room-update") // 프론트엔드가 이벤트 리스너로 등록할 이름
                            .data(data));
        } catch (IOException exception) {
            sseRepository.deleteById(emitterId); // 에러 발생 시 죽은 파이프 삭제
        }
    }

    // 방 전체 유저에게 데이터를 뿌릴 때 사용할 브로드캐스트 메서드
    public void broadcastToRoom(String entryCode, Object data) {
        Map<String, SseEmitter> roomEmitters = sseRepository.findAllByEntryCode(entryCode);
        roomEmitters.forEach((id, emitter) -> sendToClient(emitter, id, data));
    }
}
