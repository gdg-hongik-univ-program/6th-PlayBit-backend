package com.playbit.backend.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.member.dto.GetStatsResponse;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("닉네임 변경 요청 시 중복되지 않으면 닉네임이 정상적으로 변경된다.")
    void setMemberNickname_success() {
        Member member = Member.builder()
                .memberId(1L)
                .googleSub("12345")
                .email("user@gmail.com")
                .nickname("구닉네임")
                .build();

        when(memberRepository.existsByNickname("새닉네임")).thenReturn(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        memberService.setMemberNickname(member, "새닉네임");

        assertThat(member.getNickname()).isEqualTo("새닉네임");
        verify(memberRepository).existsByNickname("새닉네임");
        verify(memberRepository).findById(1L);
    }

    @Test
    @DisplayName("이미 존재하는 닉네임으로 변경 시 BadRequestException이 발생한다.")
    void setMemberNickname_duplicatedNickname() {
        Member member = Member.builder()
                .memberId(1L)
                .googleSub("12345")
                .email("user@gmail.com")
                .nickname("구닉네임")
                .build();

        when(memberRepository.existsByNickname("중복닉네임")).thenReturn(true);

        assertThatThrownBy(() -> memberService.setMemberNickname(member, "중복닉네임"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("회원의 미션 통계를 정상적으로 조회한다.")
    void getMemberStats_success() {
        Member member = Member.builder()
                .memberId(1L)
                .googleSub("12345")
                .email("user@gmail.com")
                .nickname("플레이비트테스터")
                .totalMissionSuccess(5)
                .consecutiveMissionStreak(3)
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        GetStatsResponse response = memberService.getMemberStats(member);

        assertThat(response).isNotNull();
        assertThat(response.nickname()).isEqualTo("플레이비트테스터");
        assertThat(response.totalMissionSuccess()).isEqualTo(5);
        assertThat(response.consecutiveMissionStreak()).isEqualTo(3);
    }
}