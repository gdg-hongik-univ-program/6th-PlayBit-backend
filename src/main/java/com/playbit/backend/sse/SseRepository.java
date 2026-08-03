package com.playbit.backend.sse;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class SseRepository {
    // key 구조 : {entryCode}_{memberId} (예: ABC123_1)
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 파이프 저장
    public void save(String emitterId, SseEmitter sseEmitter){
        emitters.put(emitterId, sseEmitter);
    }

    // 파이프 삭제 (연결이 끊기면 호출)
    public void deleteById(String emitterId){
        emitters.remove(emitterId);
    }

    // 특정 방(entryCode)에 접속해 있는 모든 유저의 파이프를 찾는 메서드
    public Map<String, SseEmitter> findAllByEntryCode(String entryCode) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(entryCode + "_"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
