package com.playbit.backend.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("새로운 회원이 성공적으로 생성되고 UUID가 반환되어야 한다")
    void createMember_success() {
        //given

        //when
        MemberDTO result = memberService.createMember();

        //then
        //생성된 DTO가 null이 아닌지, uuid가 null이 아닌지 확인
        assertThat(result) .isNotNull();
        assertThat(result.uuid()).isNotNull();

        // memberRepository.save()를 호출할 때 날아가는 엔티티 담고 검증
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository, times(1)).save(memberCaptor.capture());

        Member savedMember = memberCaptor.getValue();

        // 실제 저장하려는 엔티티의 uuid와 생성한 DTO의 uuid가 같은지 검증
        assertThat(savedMember.getMemberUuid()).isEqualTo(result.uuid().toString());
    }
}
