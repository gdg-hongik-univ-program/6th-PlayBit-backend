package com.playbit.backend.common.event;

import com.playbit.backend.member.Member;

/** Event emitted when a member successfully completes a mission. */
public record MissionSuccessEvent(String roomCode, Member member) {}
