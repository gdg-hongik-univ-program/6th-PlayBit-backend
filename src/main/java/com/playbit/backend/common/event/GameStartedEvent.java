package com.playbit.backend.common.event;

import java.util.List;

public record GameStartedEvent(String entryCode, List<Long> memberIds) {}
