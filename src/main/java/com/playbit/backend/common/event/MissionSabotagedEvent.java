package com.playbit.backend.common.event;

import java.util.List;

public record MissionSabotagedEvent(String roomCode, List<Long> memberIds) {}
