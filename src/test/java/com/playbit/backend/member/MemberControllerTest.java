package com.playbit.backend.member;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playbit.backend.auth.LoginMemberArgumentResolver;
import com.playbit.backend.auth.MemberAuthInterceptor;
import com.playbit.backend.member.dto.GetStatsResponse;
import com.playbit.backend.member.dto.SetNicknameRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MemberController.class)
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private MemberAuthInterceptor memberAuthInterceptor;

    @MockitoBean
    private LoginMemberArgumentResolver loginMemberArgumentResolver;

    private Member loginMember;

    @BeforeEach
    void setUp() throws Exception {
        loginMember = Member.builder()
                .memberId(1L)
                .googleSub("123456789")
                .email("user@gmail.com")
                .nickname("플레이비트테스터")
                .build();

        given(memberAuthInterceptor.preHandle(any(), any(), any())).willReturn(true);
        given(loginMemberArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginMemberArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(loginMember);
    }

    @Test
    @DisplayName("로그인된 회원의 닉네임을 성공적으로 수정한다.")
    void setMemberNickname_success() throws Exception {
        SetNicknameRequest request = new SetNicknameRequest("새닉네임");

        mockMvc.perform(patch("/api/members/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("로그인된 회원의 미션 통계를 조회한다.")
    void getMemberStats_success() throws Exception {
        GetStatsResponse response = new GetStatsResponse("플레이비트테스터", 5, 3);
        given(memberService.getMemberStats(any())).willReturn(response);

        mockMvc.perform(get("/api/members/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("플레이비트테스터"))
                .andExpect(jsonPath("$.data.totalMissionSuccess").value(5))
                .andExpect(jsonPath("$.data.consecutiveMissionStreak").value(3));
    }
}