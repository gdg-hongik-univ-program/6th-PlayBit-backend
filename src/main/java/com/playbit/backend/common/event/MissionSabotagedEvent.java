package com.playbit.backend.common.event;

import com.playbit.backend.member.Member;
import java.util.List;

public record MissionSabotagedEvent(String roomCode, List<Member> members) {}
