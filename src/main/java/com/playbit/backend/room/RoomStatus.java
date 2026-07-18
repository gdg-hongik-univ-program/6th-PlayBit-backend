package com.playbit.backend.room;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomStatus {
    WAITING,
    PLAYING,
    FINISHED;

    // ★ Jackson이 이 Enum을 JSON으로 만들거나 직렬화할 때
    // 객체 대신 "PLAYING", "FINISHED" 같은 문자열(name)로 취급하도록 강제합니다!
    @JsonValue
    public String getValue() {
        return this.name();
    }
}
