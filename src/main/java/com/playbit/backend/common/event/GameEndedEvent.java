package com.playbit.backend.common.event;

import java.util.List;

public record GameEndedEvent(String roomCode, List<Long> memberIds) {}
