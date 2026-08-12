package com.playbit.backend.common.event;

import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MissionSuccessListener {

    private final MemberRepository memberRepository;

    @EventListener
    @Transactional
    public void handleMissionSuccess(MissionSuccessEvent event) {
        Member member = memberRepository
                .findByMemberUuid(event.member().getMemberUuid())
                .orElseThrow(() -> new IllegalStateException("Member not found for MissionSuccessEvent"));
        member.incrementMissionSuccess();
        member.updateMissionStreak();
    }
}
