package com.playbit.backend.member;

import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.dto.CreateMemberResponse;
import com.playbit.backend.member.dto.GetStatsResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public CreateMemberResponse createMember() {
        UUID uuid = UUID.randomUUID();

        // uuid 중복 확률은 극히 드물어 성능을 위해 중복 검사 로직 생략하고 바로 등록
        memberRepository.save(new Member(uuid.toString()));

        return new CreateMemberResponse(uuid, null);
    }

    @Transactional
    public void setMemberNickname(String memberUuid, String nickname) {
        Member member = memberRepository
                .findByMemberUuid(memberUuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        if (memberRepository.existsByNickname(nickname)) {
            throw new BadRequestException(ErrorCode.NICKNAME_DUPLICATED);
        }

        member.updateNickname(nickname);
    }

    @Transactional(readOnly = true)
    public GetStatsResponse getMemberStats(String memberUuid) {
        Member member = memberRepository
                .findByMemberUuid(memberUuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        return new GetStatsResponse(
                member.getNickname(), member.getTotalMissionSuccess(), member.getConsecutiveMissionStreak());
    }
}
