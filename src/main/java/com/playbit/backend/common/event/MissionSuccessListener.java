package com.playbit.backend.common.event;

import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MissionSuccessListener {

    private final MemberRepository memberRepository;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 독립 트랜잭션으로 즉시 DB 커밋 보장
    public void handleMissionSuccess(MissionSuccessEvent event) {
        Member managedMember = memberRepository.findById(event.memberId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        managedMember.incrementMissionSuccess();
        managedMember.updateMissionStreak();
    }
}