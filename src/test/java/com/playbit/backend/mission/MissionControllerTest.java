package com.playbit.backend.mission;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.playbit.backend.auth.LoginMemberArgumentResolver;
import com.playbit.backend.auth.MemberAuthInterceptor;
import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.mission.dto.MissionCompleteResponse;
import com.playbit.backend.mission.dto.MissionDto;
import com.playbit.backend.mission.dto.MissionSabotageResponse;
import com.playbit.backend.room.Category;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomStatus;
import com.playbit.backend.room.dto.FinishedRoomDto;
import com.playbit.backend.room.dto.PlayingRoomDto;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MissionController.class)
public class MissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MissionService missionService;

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
                .nickname("테스트유저")
                .build();

        given(memberAuthInterceptor.preHandle(any(), any(), any())).willReturn(true);
        given(loginMemberArgumentResolver.supportsParameter(any())).willReturn(true);
        given(loginMemberArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(loginMember);
    }

    @Test
    @DisplayName("올바른 사용자가 미션 완료 요청을 보냈고, 게임이 끝나지 않아 계속 진행된다.")
    void completeMissionTest_gameNotEnded() throws Exception {

        String entryCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Room room = new Room(3L, RoomStatus.PLAYING, entryCode, null, Category.STUDY, "테스트방", 3L, 4L, null, null, false, null);

        Mission mission = new Mission(
                8L,
                room,
                3L,
                Content.STUDY_1,
                loginMember,
                LocalDateTime.now(),
                "https://test-image-url.jpg",
                false,
                null,
                "완료 코멘트",
                null);

        given(missionService.completeMission(any(), anyLong(), anyString(), any(), any()))
                .willReturn(new MissionCompleteResponse(PlayingRoomDto.from(room), MissionDto.from(mission)));

        MockMultipartFile image =
                new MockMultipartFile("image", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes());

        mockMvc.perform(multipart("/api/rooms/{entryCode}/missions/{position}", entryCode, 3L)
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mission.position").value(3L))
                .andExpect(jsonPath("$.data.mission.completedByMemberId").value(loginMember.getMemberId()))
                .andExpect(jsonPath("$.data.mission.comment").value("완료 코멘트"))
                .andExpect(jsonPath("$.data.room.currentTurnMemberId").value(3L))
                .andExpect(jsonPath("$.data.room.currentTurnNumber").value(4L))
                .andExpect(jsonPath("$.data.room.currentTurnSabotaged").value(false))
                .andExpect(jsonPath("$.data.room.status").value(RoomStatus.PLAYING.toString()));
    }

    @Test
    @DisplayName("올바른 사용자가 미션 완료 요청을 보냈고, 승패가 결정되어 게임이 끝났다.")
    void completeMissionTest_gameEnded_Not_Draw() throws Exception {

        String entryCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Room room =
                new Room(90L, RoomStatus.FINISHED, entryCode, loginMember, Category.STUDY, "테스트방", 5L, 4L, null, null, false, null);

        Mission mission3 = new Mission(
                813L,
                room,
                3L,
                Content.STUDY_1,
                loginMember,
                LocalDateTime.now(),
                "https://test-image-url.jpg",
                false,
                null,
                null,
                null);

        given(missionService.completeMission(any(), anyLong(), anyString(), any(), any()))
                .willReturn(new MissionCompleteResponse(FinishedRoomDto.from(room), MissionDto.from(mission3)));

        MockMultipartFile image =
                new MockMultipartFile("image", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes());

        mockMvc.perform(multipart("/api/rooms/{entryCode}/missions/{position}", entryCode, 3L)
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mission.position").value(3L))
                .andExpect(jsonPath("$.data.mission.completedByMemberId").value(loginMember.getMemberId()))
                .andExpect(jsonPath("$.data.room.status").value(RoomStatus.FINISHED.toString()))
                .andExpect(jsonPath("$.data.room.winnerMemberId").value(loginMember.getMemberId()))
                .andExpect(jsonPath("$.data.room.isDraw").value(false));
    }

    @Test
    @DisplayName("올바른 사용자가 사보타주 요청을 보냈고, 상대방의 제한 시간이 6시간 감소하였다.")
    void sabotageMission_success() throws Exception {

        Member opponent = Member.builder().memberId(5L).googleSub("987654321").email("op@gmail.com").build();
        String entryCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        LocalDateTime completedAt = LocalDateTime.now();

        Room room = new Room(
                12L,
                RoomStatus.PLAYING,
                entryCode,
                null,
                Category.STUDY,
                "테스트방",
                3L,
                4L,
                completedAt,
                completedAt.minusHours(6L),
                true,
                null);

        Mission mission = new Mission(
                15L,
                room,
                3L,
                Content.STUDY_1,
                opponent,
                completedAt,
                "https://test-mission-image.jpg",
                true,
                "https://test-sabotage-image.jpg",
                "미션 코멘트",
                "사보타주 코멘트");

        given(missionService.sabotageMission(any(), anyLong(), anyString(), any(), any()))
                .willReturn(new MissionSabotageResponse(PlayingRoomDto.from(room), MissionDto.from(mission)));

        MockMultipartFile image = new MockMultipartFile(
                "image", "sabotage.jpg", MediaType.IMAGE_JPEG_VALUE, "sabotage-image-content".getBytes());

        mockMvc.perform(multipart("/api/rooms/{entryCode}/missions/{position}/sabotage", entryCode, 3L)
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.room.currentTurnMemberId").value(3L))
                .andExpect(jsonPath("$.data.room.currentTurnNumber").value(4L))
                .andExpect(jsonPath("$.data.room.currentTurnSabotaged").value(true))
                .andExpect(jsonPath("$.data.room.status").value(RoomStatus.PLAYING.toString()))
                .andExpect(jsonPath("$.data.mission.position").value(3L))
                .andExpect(jsonPath("$.data.mission.completedByMemberId").value(opponent.getMemberId()))
                .andExpect(jsonPath("$.data.mission.sabotagedByOpponent").value(true))
                .andExpect(jsonPath("$.data.mission.sabotageImageUrl").value("https://test-sabotage-image.jpg"))
                .andExpect(jsonPath("$.data.mission.sabotageComment").value("사보타주 코멘트"));
    }
}