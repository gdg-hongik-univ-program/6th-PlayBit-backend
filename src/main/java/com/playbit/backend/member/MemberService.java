package com.playbit.backend.member;

import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.common.exception.NotFoundException;
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

        // 트랜잭션 내에서 영속(Managed) 상태의 Member 엔티티를 조회하여 변경
        Member managedMember = memberRepository.findById(member.getMemberId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        managedMember.updateNickname(nickname); // JPA 변경 감지로 DB UPDATE 쿼리 자동 실행
    }

    @Transactional(readOnly = true)
    public GetStatsResponse getMemberStats(Member member) {
        Member managedMember = memberRepository.findById(member.getMemberId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return new GetStatsResponse(
                managedMember.getNickname(),
                managedMember.getTotalMissionSuccess(),
                managedMember.getConsecutiveMissionStreak()
        );
    }
}