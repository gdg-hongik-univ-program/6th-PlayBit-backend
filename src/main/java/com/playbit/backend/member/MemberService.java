package com.playbit.backend.member;

import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.dto.MemberCreateResponse;
import com.playbit.backend.member.dto.MemberStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberCreateResponse createMember() {

        // v4 uuid 생성 (완전히 랜덤 -> 조회 로직 성능 저하 야기할 가능성이 있음)
        UUID uuid = UUID.randomUUID();

        // uuid 중복 확률은 극히 드물어 성능을 위해 중복 검사 로직 생략하고 바로 등록
        memberRepository.save(new Member(uuid.toString()));

        return new MemberCreateResponse(uuid, null);
    }

    @Transactional
    public void setMemberNickname(String memberUuid, String nickname) {
        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new NotFoundException(com.playbit.backend.common.exception.ErrorCode.MEMBER_NOT_FOUND));
        member.setNickname(nickname);
    }

    @Transactional(readOnly = true)
    public MemberStatsResponse getMemberStats(String memberUuid) {
        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new NotFoundException(com.playbit.backend.common.exception.ErrorCode.MEMBER_NOT_FOUND));
        return new MemberStatsResponse(
                member.getNickname(),
                member.getTotalMissionSuccess(),
                member.getConsecutiveMissionStreak()
        );
    }
}

