package com.playbit.backend.room;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomStatus {
    WAITING,
    PLAYING,
    FINISHED;
}
