package com.playbit.backend.common.event;

import java.util.List;

public record MissionCompletedEvent(String roomCode, List<Long> memberIds) {
}
