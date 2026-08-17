package com.playbit.backend.common.event;

/** Event emitted when a member successfully completes a mission. */
public record MissionSuccessEvent(String roomCode, Long memberId) {}
