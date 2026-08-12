package com.playbit.backend.member;

import com.playbit.backend.common.event.MissionSuccessEvent;
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
        // Fetch fresh member entity to ensure we are in a managed context
        Member member =
                memberRepository
                        .findByMemberUuid(event.member().getMemberUuid())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Member not found for MissionSuccessEvent"));

        // Update total mission success count
        member.incrementMissionSuccess();
        // Update streak based on last success date
        member.updateMissionStreak();
    }
}
