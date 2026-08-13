package com.playbit.backend.member;

import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.member.dto.GetStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void setMemberNickname(Member member, String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new BadRequestException(ErrorCode.NICKNAME_DUPLICATED);
        }

        member.updateNickname(nickname);
    }

    @Transactional(readOnly = true)
    public GetStatsResponse getMemberStats(Member member) {
        return new GetStatsResponse(
                member.getNickname(), member.getTotalMissionSuccess(), member.getConsecutiveMissionStreak());
    }
}