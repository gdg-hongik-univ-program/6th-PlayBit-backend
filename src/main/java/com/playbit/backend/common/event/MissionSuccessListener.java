package com.playbit.backend.common.event;

import com.playbit.backend.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MissionSuccessListener {

    @EventListener
    @Transactional
    public void handleMissionSuccess(MissionSuccessEvent event) {
        Member member = event.member();
        member.incrementMissionSuccess();
        member.updateMissionStreak();
    }
}